package com.example.erp.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Long, String> getPaymentStatusByVisitIds(List<Visit> visits) {
        if (visits == null || visits.isEmpty()) {
            return Map.of();
        }

        List<Long> visitIds = visits.stream()
                .map(Visit::getVisit_id)
                .toList();

        Map<Long, Payment> paymentByVisitId = new HashMap<>();
        for (Payment p : paymentRepository.findByVisitIds(visitIds)) {
            if (p.getVisit() != null && p.getVisit().getVisit_id() != null) {
                paymentByVisitId.put(p.getVisit().getVisit_id(), p);
            }
        }

        Map<Long, String> statusByVisitId = new HashMap<>();
        for (Visit v : visits) {
            Payment p = paymentByVisitId.get(v.getVisit_id());
            String statusName = "미결제";
            if (p != null && p.getStatus_code() != null) {
                statusName = p.getStatus_code().getName();
            }
            statusByVisitId.put(v.getVisit_id(), statusName);
        }

        return statusByVisitId;
    }

    public PaymentPageModel loadPaymentPage(Long visitId) {
        if (visitId == null)
            return PaymentPageModel.empty();

        Visit visit = visitRepository.findDetail(visitId);
        if (visit == null)
            return PaymentPageModel.empty();

        List<Claim> claims = claimRepository.findAllByVisitId(visitId);
        Claim claim = claims.isEmpty() ? null : claims.get(0);
        Payment payment = paymentRepository.findByVisitId(visitId).orElse(null);

        int itemsTotal = 0;
        int itemsDiscount = 0;
        int itemCount = 0;

        List<Claim_item> items = List.of();
        if (!claims.isEmpty()) {
            items = claimItemRepository.findAllByVisitId(visitId);

            // 보험 할인율(예: 0.3 = 30%)
            BigDecimal rate = BigDecimal.ZERO;
            Insurance_code ins = visit.getInsurance_code();
            if (ins != null) {
                rate = BigDecimal.valueOf(ins.getDiscount_rate());
            }

            Map<Long, Integer> totalsByClaim = new HashMap<>();
            Map<Long, Integer> discountsByClaim = new HashMap<>();

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

                Claim itemClaim = ci.getClaim();
                if (itemClaim != null && itemClaim.getClaim_id() != null) {
                    Long claimId = itemClaim.getClaim_id();
                    totalsByClaim.merge(claimId, ci.getTotal(), Integer::sum);
                    discountsByClaim.merge(claimId, ci.getDiscount(), Integer::sum);
                }

                itemsTotal += ci.getTotal();
                itemsDiscount += ci.getDiscount();
            }
            if (!items.isEmpty()) {
                claimItemRepository.saveAll(items);
            }
            // 청구 합계도 갱신
            for (Claim c : claims) {
                Integer total = totalsByClaim.get(c.getClaim_id());
                Integer discount = discountsByClaim.get(c.getClaim_id());
                if (total != null) {
                    c.setTotal_amount(total);
                }
                if (discount != null) {
                    c.setDiscount_amount(discount);
                }
            }
            if (!claims.isEmpty()) {
                claimRepository.saveAll(claims);
            }

            itemCount = items.size();
        }

        // 합계는 항목 합산값 기준 (보험 할인은 항목 discount에 반영됨)
        int totalAmount = itemsTotal;
        int baseDiscount = itemsDiscount;
        int insuranceDiscount = 0;
        int discountAmount = baseDiscount + insuranceDiscount;
        int finalAmount = Math.max(0, totalAmount - discountAmount);

        List<Payment_method> methods = paymentMethodRepository.findAll();
        boolean claimReady = !claims.isEmpty() && itemCount > 0;
        String paymentStatusName = payment != null && payment.getStatus_code() != null
                ? payment.getStatus_code().getName()
                : "미결제";
        boolean paymentCompleted = payment != null
                && payment.getStatus_code() != null
                && "PAY_COMPLETED".equalsIgnoreCase(payment.getStatus_code().getStatus_code());

        return new PaymentPageModel(visit, claim, payment, paymentStatusName, paymentCompleted, totalAmount,
                baseDiscount, insuranceDiscount, discountAmount, finalAmount, methods, claimReady, itemCount, items);
    }

    @Transactional
    public void pay(Long visitId, String paymentMethodCode) {
        if (paymentRepository.existsByVisitId(visitId))
            return;

        PaymentPageModel m = loadPaymentPage(visitId);
        Visit visit = m.visit();
        if (visit == null)
            throw new IllegalArgumentException("visit not found");
        if (!m.claimReady())
            throw new IllegalStateException("청구/청구항목이 없습니다. 결제 전에 청구를 생성해 주세요.");

        Payment payment = new Payment();
        payment.setVisit(visit);

        Payment_method pm = paymentMethodRepository.findById(paymentMethodCode)
                .orElseThrow(() -> new IllegalArgumentException("payment_method_code not found"));
        payment.setPayment_method(pm);

        payment.setAmount(m.finalAmount());
        payment.setPaid_at(LocalDateTime.now());

        Status_code paid = statusCodeRepository.findById("PAY_COMPLETED")
                .orElseThrow(() -> new IllegalArgumentException("status_code PAYMENT_DONE not found"));
        payment.setStatus_code(paid);

        paymentRepository.save(payment);
    }

    @Transactional
    public void refund(Long visitId) {
        Payment payment = paymentRepository.findByVisitId(visitId)
                .orElseThrow(() -> new IllegalArgumentException("payment not found"));
        if (payment.getStatus_code() == null
                || !"PAY_COMPLETED".equalsIgnoreCase(payment.getStatus_code().getStatus_code())) {
            return;
        }

        Status_code refunded = statusCodeRepository.findById("PAY_REFUND")
                .orElseThrow(() -> new IllegalArgumentException("status_code PAY_REFUND not found"));
        payment.setStatus_code(refunded);
        paymentRepository.save(payment);
    }

    public record PaymentPageModel(
            Visit visit,
            Claim claim,
            Payment payment,
            String paymentStatusName,
            boolean paymentCompleted,
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
            return new PaymentPageModel(null, null, null, "미결제", false, 0, 0, 0, 0, 0, List.of(), false, 0,
                    List.of());
        }
    }
}
