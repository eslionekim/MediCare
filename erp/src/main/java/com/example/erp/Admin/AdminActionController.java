package com.example.erp.Admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/actions")
@RequiredArgsConstructor
public class AdminActionController {

    private final AdminApprovalService adminApprovalService;
    private final AdminRevenueCycleService adminRevenueCycleService;
    private final AdminReceivableService adminReceivableService;
    private final AdminUserService adminUserService;

    @PostMapping("/purchase-approval/{requestId}/approve")
    public ResponseEntity<Void> approvePurchase(@PathVariable("requestId") Long requestId) {
        adminApprovalService.updatePurchaseStatus(requestId, "IR_APPROVED");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase-approval/{requestId}/reject")
    public ResponseEntity<Void> rejectPurchase(@PathVariable("requestId") Long requestId) {
        adminApprovalService.updatePurchaseStatus(requestId, "IR_REJECTED");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/price-exception/{itemCode}/approve")
    public ResponseEntity<Void> approvePriceException(@PathVariable("itemCode") String itemCode) {
        adminApprovalService.updateItemActive(itemCode, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/price-exception/{itemCode}/reject")
    public ResponseEntity<Void> rejectPriceException(@PathVariable("itemCode") String itemCode) {
        adminApprovalService.updateItemActive(itemCode, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock-lockin/{moveId}/confirm")
    public ResponseEntity<Void> confirmStockLockin(@PathVariable("moveId") Long moveId) {
        adminApprovalService.updateStockMoveStatus(moveId, "SM_DONE");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock-lockin/{moveId}/release")
    public ResponseEntity<Void> releaseStockLockin(@PathVariable("moveId") Long moveId) {
        adminApprovalService.updateStockMoveStatus(moveId, "SM_DRAFT");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revenue-cycle/{visitId}/status-correction")
    public ResponseEntity<Void> requestStatusCorrection(@PathVariable("visitId") Long visitId) {
        adminRevenueCycleService.requestStatusCorrection(visitId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revenue-cycle/{visitId}/payment-claim-correction")
    public ResponseEntity<Void> requestPaymentClaimCorrection(@PathVariable("visitId") Long visitId) {
        adminRevenueCycleService.requestPaymentClaimCorrection(visitId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/receivable/{visitId}/toggle-postpaid")
    public ResponseEntity<Void> togglePostpaid(@PathVariable("visitId") Long visitId) {
        adminReceivableService.togglePostpaid(visitId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/receivable/{visitId}/collect")
    public ResponseEntity<Void> collectReceivable(@PathVariable("visitId") Long visitId) {
        adminReceivableService.markCollected(visitId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/toggle-active")
    public ResponseEntity<Void> toggleUserActive(@PathVariable("userId") String userId) {
        adminUserService.toggleUserActive(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable("userId") String userId,
            @RequestParam(value = "password", required = false) String password) {
        adminUserService.resetPassword(userId, password != null ? password : "1234");
        return ResponseEntity.ok().build();
    }
}
