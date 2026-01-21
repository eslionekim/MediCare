package com.example.erp.Payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.erp.Payment.PaymentService.PaymentPageModel;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public String page(@RequestParam(value = "visitId", required = false) Long visitId, Model model) {

        var visits = paymentService.getAllVisits();
        model.addAttribute("todayVisits", visits);
        model.addAttribute("paymentStatusByVisitId", paymentService.getPaymentStatusByVisitIds(visits));

        PaymentPageModel m = paymentService.loadPaymentPage(visitId);

        model.addAttribute("visit", m.visit());
        model.addAttribute("payment", m.payment());
        model.addAttribute("paymentStatusName", m.paymentStatusName());
        model.addAttribute("paymentCompleted", m.paymentCompleted());
        model.addAttribute("totalAmount", m.totalAmount());
        model.addAttribute("baseDiscount", m.baseDiscount());
        model.addAttribute("insuranceDiscount", m.insuranceDiscount());
        model.addAttribute("discountAmount", m.discountAmount());
        model.addAttribute("finalAmount", m.finalAmount());
        model.addAttribute("paymentMethods", m.paymentMethods());
        model.addAttribute("claimReady", m.claimReady());
        model.addAttribute("itemCount", m.itemCount());
        model.addAttribute("claimItems", m.claimItems());
        model.addAttribute("feeItems", paymentService.getFeeItemOptions());

        return "staff/payment";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam("visitId") Long visitId,
            @RequestParam("paymentMethodCode") String paymentMethodCode,
            RedirectAttributes redirectAttributes) {

        try {
            paymentService.pay(visitId, paymentMethodCode);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("paymentError", e.getMessage());
        }
        return "redirect:/payments?visitId=" + visitId;
    }

    @PostMapping("/refund")
    public String refund(@RequestParam("visitId") Long visitId) {
        paymentService.refund(visitId);
        return "redirect:/payments?visitId=" + visitId;
    }

    @PostMapping("/claim-items")
    public String updateClaimItems(
            @RequestParam("visitId") Long visitId,
            @RequestParam(value = "itemId", required = false) java.util.List<Long> itemIds,
            @RequestParam(value = "quantity", required = false) java.util.List<Integer> quantities,
            @RequestParam(value = "deleteIds", required = false) java.util.List<Long> deleteIds,
            @RequestParam(value = "newFeeItemCode", required = false) java.util.List<String> newFeeItemCodes,
            @RequestParam(value = "newQuantity", required = false) java.util.List<Integer> newQuantities) {
        paymentService.updateClaimItems(visitId, itemIds, quantities, deleteIds, newFeeItemCodes, newQuantities);
        return "redirect:/payments?visitId=" + visitId;
    }
}
