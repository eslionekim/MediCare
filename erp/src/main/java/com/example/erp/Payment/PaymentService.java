package com.example.erp.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Claim.Claim;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Insurance_code.Insurance_code;
import com.example.erp.Payment_method.Payment_method;
import com.example.erp.Payment_method.Payment_methodRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Visit.Visit;
import com.example.erp.Visit.VisitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VisitRepository visitRepository;
    private final ClaimRepository claimRepository;
    private final Claim_itemRepository claimItemRepository;
    private final PaymentRepository paymentRepository;
    private final Payment_methodRepository paymentMethodRepository;
    private final Status_codeRepository statusCodeRepository;

    public List<Visit> getTodayVisits() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);
        return visitRepository.findTodaysVisits(start, end);
    }

    public PaymentPageModel loadPaymentPage(Long visitId) {
        if (visitId == null) return PaymentPageModel.empty();

        Visit visit = visitRepository.findDetail(visitId);
        if (visit == null) return PaymentPageModel.empty();

        Claim claim = claimRepository.findByVisitId(visitId).orElse(null);

        int itemsTotal = 0;
        int itemsDiscount = 0;
        int itemCount = 0;

        List<Claim_item> items = List.of();
        if (claim != null) {
            items = claimItemRepository.findByClaim(claim);

            // 보험 할인율(예: 0.3 = 30%)
            BigDecimal rate = BigDecimal.ZERO;
            Insurance_code ins = visit.getInsurance_code();
            if (ins != null) {
                rate = BigDecimal.valueOf(ins.getDiscount_rate());
            }

            for (Claim_item ci : items) {
                int price = ci.getUnit_price() * ci.getQuantity();
                int discount = 0;
                if (rate.compareTo(BigDecimal.ZERO) > 0 && ci.getFee_item() != null && ci.getFee_item().is_active()) {
                    discount = BigDecimal.valueOf(price)
                            .multiply(rate)
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                }
                ci.setDiscount(discount);
                ci.setTotal(Math.max(0, price - discount));

                itemsTotal += ci.getTotal();
                itemsDiscount += ci.getDiscount();
            }
            if (!items.isEmpty()) {
                claimItemRepository.saveAll(items);
            }
            // 청구 합계도 갱신
            claim.setTotal_amount(itemsTotal);
            claim.setDiscount_amount(itemsDiscount);
            claimRepository.save(claim);

            itemCount = items.size();
        }

        // 합계는 항목 합산값 기준 (보험 할인은 항목 discount에 반영됨)
        int totalAmount = itemsTotal;
        int baseDiscount = itemsDiscount;
        int insuranceDiscount = 0;
        int discountAmount = baseDiscount + insuranceDiscount;
        int finalAmount = Math.max(0, totalAmount - discountAmount);

        List<Payment_method> methods = paymentMethodRepository.findAll();
        boolean claimReady = claim != null && itemCount > 0;

        return new PaymentPageModel(visit, claim, totalAmount, baseDiscount, insuranceDiscount, discountAmount,
                finalAmount, methods, claimReady, itemCount, items);
    }

    @Transactional
    public void pay(Long visitId, String paymentMethodCode) {
        if (paymentRepository.existsByVisitId(visitId)) return;

        PaymentPageModel m = loadPaymentPage(visitId);
        Visit visit = m.visit();
        if (visit == null) throw new IllegalArgumentException("visit not found");
        if (!m.claimReady()) throw new IllegalStateException("청구/청구항목이 없습니다. 결제 전에 청구를 생성해 주세요.");

        Payment payment = new Payment();
        payment.setVisit(visit);

        Payment_method pm = paymentMethodRepository.findById(paymentMethodCode)
                .orElseThrow(() -> new IllegalArgumentException("payment_method_code not found"));
        payment.setPayment_method(pm);

        payment.setAmount(m.finalAmount());

        Status_code paid = statusCodeRepository.findById("PAYMENT_DONE")
                .orElseThrow(() -> new IllegalArgumentException("status_code PAYMENT_DONE not found"));
        payment.setStatus_code(paid);

        paymentRepository.save(payment);
    }

    public record PaymentPageModel(
            Visit visit,
            Claim claim,
            int totalAmount,
            int baseDiscount,
            int insuranceDiscount,
            int discountAmount,
            int finalAmount,
            List<Payment_method> paymentMethods,
            boolean claimReady,
            int itemCount,
            List<Claim_item> claimItems) {
        static PaymentPageModel empty() {
            return new PaymentPageModel(null, null, 0, 0, 0, 0, 0, List.of(), false, 0, List.of());
        }
    }
}
