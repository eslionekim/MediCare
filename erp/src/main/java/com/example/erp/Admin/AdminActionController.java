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
    private final AdminCodeService adminCodeService;
    private final AdminWorkService adminWorkService;

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

    @PostMapping("/code/department/create")
    public ResponseEntity<Void> createDepartment(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.createDepartment(code, name, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/department/update")
    public ResponseEntity<Void> updateDepartment(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.updateDepartment(code, name, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/department/deactivate")
    public ResponseEntity<Void> deactivateDepartment(@RequestParam("code") String code) {
        adminCodeService.deactivateDepartment(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/insurance/create")
    public ResponseEntity<Void> createInsurance(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("discountRate") String discountRate,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.createInsurance(code, name, discountRate, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/insurance/update")
    public ResponseEntity<Void> updateInsurance(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("discountRate") String discountRate,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.updateInsurance(code, name, discountRate, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/insurance/deactivate")
    public ResponseEntity<Void> deactivateInsurance(@RequestParam("code") String code) {
        adminCodeService.deactivateInsurance(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/payment-method/create")
    public ResponseEntity<Void> createPaymentMethod(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.createPaymentMethod(code, name, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/payment-method/update")
    public ResponseEntity<Void> updatePaymentMethod(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.updatePaymentMethod(code, name, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/payment-method/deactivate")
    public ResponseEntity<Void> deactivatePaymentMethod(@RequestParam("code") String code) {
        adminCodeService.deactivatePaymentMethod(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/fee-item/create")
    public ResponseEntity<Void> createFeeItem(
            @RequestParam("code") String code,
            @RequestParam("category") String category,
            @RequestParam("name") String name,
            @RequestParam("basePrice") String basePrice,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.createFeeItem(code, category, name, basePrice, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/fee-item/update")
    public ResponseEntity<Void> updateFeeItem(
            @RequestParam("code") String code,
            @RequestParam("category") String category,
            @RequestParam("name") String name,
            @RequestParam("basePrice") String basePrice,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.updateFeeItem(code, category, name, basePrice, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/fee-item/deactivate")
    public ResponseEntity<Void> deactivateFeeItem(@RequestParam("code") String code) {
        adminCodeService.deactivateFeeItem(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/diagnosis/create")
    public ResponseEntity<Void> createDiagnosis(
            @RequestParam("code") String code,
            @RequestParam("nameKor") String nameKor,
            @RequestParam("nameEng") String nameEng,
            @RequestParam("department") String department,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.createDisease(code, nameKor, nameEng, department, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/diagnosis/update")
    public ResponseEntity<Void> updateDiagnosis(
            @RequestParam("code") String code,
            @RequestParam("nameKor") String nameKor,
            @RequestParam("nameEng") String nameEng,
            @RequestParam("department") String department,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.updateDisease(code, nameKor, nameEng, department, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/diagnosis/deactivate")
    public ResponseEntity<Void> deactivateDiagnosis(@RequestParam("code") String code) {
        adminCodeService.deactivateDisease(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/status/create")
    public ResponseEntity<Void> createStatus(
            @RequestParam("code") String code,
            @RequestParam("category") String category,
            @RequestParam("name") String name,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.createStatus(code, category, name, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/status/update")
    public ResponseEntity<Void> updateStatus(
            @RequestParam("code") String code,
            @RequestParam("category") String category,
            @RequestParam("name") String name,
            @RequestParam(value = "active", required = false) String active) {
        adminCodeService.updateStatus(code, category, name, active);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/code/status/deactivate")
    public ResponseEntity<Void> deactivateStatus(@RequestParam("code") String code) {
        adminCodeService.deactivateStatus(code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/work-type/create")
    public ResponseEntity<Void> createWorkType(
            @RequestParam("workTypeCode") String workTypeCode,
            @RequestParam("roleCode") String roleCode,
            @RequestParam("workName") String workName,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "note", required = false) String note) {
        adminWorkService.createWorkType(workTypeCode, roleCode, workName, startTime, endTime, note);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/work-type/update")
    public ResponseEntity<Void> updateWorkType(
            @RequestParam("workTypeCode") String workTypeCode,
            @RequestParam("roleCode") String roleCode,
            @RequestParam("workName") String workName,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam(value = "note", required = false) String note) {
        adminWorkService.updateWorkType(workTypeCode, roleCode, workName, startTime, endTime, note);
        return ResponseEntity.ok().build();
    }
}
