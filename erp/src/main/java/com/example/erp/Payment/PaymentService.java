package com.example.erp.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Claim.Claim;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Fee_item.Fee_itemRepository;
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
    private final Fee_itemRepository feeItemRepository;

    public List<Visit> getTodayVisits() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);
        return visitRepository.findTodaysVisits(start, end);
    }

    public List<Visit> getAllVisits() {
        return visitRepository.findAllVisits();
    }

    public List<Fee_item> getFeeItemOptions() {
        return feeItemRepository.findAll();
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
        Payment payment = paymentRepository.findLatestByVisitId(visitId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);

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
                int patientPay = price;
                boolean covered = ci.getFee_item() != null && ci.getFee_item().is_active();
                if (covered && rate.compareTo(BigDecimal.ZERO) >= 0) {
                    patientPay = BigDecimal.valueOf(price)
                            .multiply(rate)
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                    discount = Math.max(0, price - patientPay);
                }
                ci.setDiscount(discount);
                ci.setTotal(Math.max(0, patientPay));

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
        int totalAmount = itemsTotal + itemsDiscount;
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
    public void updateClaimItems(Long visitId,
            List<Long> itemIds,
            List<Integer> quantities,
            List<Long> deleteIds,
            List<String> newFeeItemCodes,
            List<Integer> newQuantities) {
        if (visitId == null) {
            return;
        }

        Payment existing = paymentRepository.findLatestByVisitId(visitId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
        if (existing != null
                && existing.getStatus_code() != null
                && "PAY_COMPLETED".equalsIgnoreCase(existing.getStatus_code().getStatus_code())) {
            throw new IllegalStateException("payment already completed");
        }

        Visit visit = visitRepository.findDetail(visitId);
        if (visit == null) {
            throw new IllegalArgumentException("visit not found");
        }

        List<Claim> claims = claimRepository.findAllByVisitId(visitId);
        Claim targetClaim = claims.isEmpty() ? null : claims.get(0);
        if (targetClaim == null) {
            Claim newClaim = new Claim();
            newClaim.setVisit(visit);
            newClaim.setCreated_at(LocalDateTime.now());
            targetClaim = claimRepository.save(newClaim);
        }

        if (itemIds != null && quantities != null && !itemIds.isEmpty()) {
            Map<Long, Claim_item> existingItems = new HashMap<>();
            for (Claim_item ci : claimItemRepository.findAllById(itemIds)) {
                if (ci.getClaim_item_id() != null) {
                    existingItems.put(ci.getClaim_item_id(), ci);
                }
            }
            for (int i = 0; i < itemIds.size(); i++) {
                Long id = itemIds.get(i);
                Claim_item ci = existingItems.get(id);
                if (ci == null) {
                    continue;
                }
                if (deleteIds != null && deleteIds.contains(id)) {
                    claimItemRepository.delete(ci);
                    continue;
                }
                int qty = quantities.size() > i && quantities.get(i) != null ? quantities.get(i) : 0;
                if (qty <= 0) {
                    qty = 1;
                }
                ci.setQuantity(qty);
                if (ci.getFee_item() != null) {
                    ci.setUnit_price(ci.getFee_item().getBase_price());
                }
            }
        }

        if (newFeeItemCodes != null && newQuantities != null) {
            int count = Math.min(newFeeItemCodes.size(), newQuantities.size());
            for (int i = 0; i < count; i++) {
                String code = newFeeItemCodes.get(i);
                Integer qtyValue = newQuantities.get(i);
                if (code == null || code.isBlank()) {
                    continue;
                }
                Fee_item feeItem = feeItemRepository.findById(code).orElse(null);
                if (feeItem == null) {
                    continue;
                }
                int qty = qtyValue != null ? qtyValue : 0;
                if (qty <= 0) {
                    qty = 1;
                }
                Claim_item newItem = new Claim_item();
                newItem.setClaim(targetClaim);
                newItem.setFee_item(feeItem);
                newItem.setUnit_price(feeItem.getBase_price());
                newItem.setQuantity(qty);
                newItem.setDiscount(0);
                newItem.setTotal(0);
                claimItemRepository.save(newItem);
            }
        }
    }

    @Transactional
    public void pay(Long visitId, String paymentMethodCode) {
        Payment existing = paymentRepository.findLatestByVisitId(visitId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
        if (existing != null
                && existing.getStatus_code() != null
                && !"PAY_REFUND".equalsIgnoreCase(existing.getStatus_code().getStatus_code())) {
            return;
        }

        PaymentPageModel m = loadPaymentPage(visitId);
        Visit visit = m.visit();
        if (visit == null)
            throw new IllegalArgumentException("visit not found");
        if (!m.claimReady())
            throw new IllegalStateException("청구/청구항목이 없습니다. 결제 전에 청구를 생성해 주세요.");

        Payment payment = existing != null ? existing : new Payment();
        payment.setVisit(visit);

        Payment_method pm = paymentMethodRepository.findById(paymentMethodCode)
                .orElseThrow(() -> new IllegalArgumentException("payment_method_code not found"));
        payment.setPayment_method(pm);

        payment.setAmount(m.finalAmount());
        payment.setPaid_at(LocalDateTime.now());

        Status_code paid = statusCodeRepository.findById("PAY_COMPLETED")
                .orElseThrow(() -> new IllegalArgumentException("status_code PAYMENT_DONE not found"));
        payment.setStatus_code(paid);

        paymentRepository.saveAndFlush(payment);
    }

    @Transactional
    public void refund(Long visitId) {
        Payment payment = paymentRepository.findLatestByVisitId(visitId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
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
