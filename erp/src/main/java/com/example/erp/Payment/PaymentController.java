package com.example.erp.Payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.erp.Payment.PaymentService.PaymentPageModel;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public String page(@RequestParam(value = "visitId", required = false) Long visitId, Model model) {

        model.addAttribute("todayVisits", paymentService.getTodayVisits());

        PaymentPageModel m = paymentService.loadPaymentPage(visitId);

        model.addAttribute("visit", m.visit());
        model.addAttribute("totalAmount", m.totalAmount());
        model.addAttribute("baseDiscount", m.baseDiscount());
        model.addAttribute("insuranceDiscount", m.insuranceDiscount());
        model.addAttribute("discountAmount", m.discountAmount());
        model.addAttribute("finalAmount", m.finalAmount());
        model.addAttribute("paymentMethods", m.paymentMethods());
        model.addAttribute("claimReady", m.claimReady());
        model.addAttribute("itemCount", m.itemCount());
        model.addAttribute("claimItems", m.claimItems());

        return "staff/payment";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam Long visitId,
            @RequestParam String paymentMethodCode) {

        paymentService.pay(visitId, paymentMethodCode);
        return "redirect:/payments?visitId=" + visitId;
    }
}
