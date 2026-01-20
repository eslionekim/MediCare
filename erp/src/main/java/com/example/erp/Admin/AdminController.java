package com.example.erp.Admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final AdminRevenueCycleService adminRevenueCycleService;
    private final AdminInventoryService adminInventoryService;
    private final AdminReportService adminReportService;
    private final AdminReceivableService adminReceivableService;
    private final AdminApprovalService adminApprovalService;
    private final AdminUserService adminUserService;
    private final AdminCodeService adminCodeService;
    private final AdminWorkService adminWorkService;

    @GetMapping("/admin/dashboard")
    public String dashboard(
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "insuranceCode", required = false) String insuranceCode,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {
        AdminDashboardService.DashboardData data = adminDashboardService.loadDashboard(
                startDate, endDate, departmentCode, doctorId, insuranceCode);
        model.addAttribute("summary", data.summary());
        model.addAttribute("deptStats", data.deptStats());
        model.addAttribute("doctorStats", data.doctorStats());
        model.addAttribute("insuranceStats", data.insuranceStats());
        model.addAttribute("stockAlerts", data.stockAlerts());
        model.addAttribute("pipeline", data.pipelineCounts());
        model.addAttribute("departmentOptions", adminDashboardService.getDepartmentOptions());
        model.addAttribute("doctorOptions", adminDashboardService.getDoctorOptions());
        model.addAttribute("insuranceOptions", adminDashboardService.getInsuranceOptions());
        model.addAttribute("selectedDepartmentCode", departmentCode);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedInsuranceCode", insuranceCode);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        return "admin/dashboard";
    }

    @GetMapping("/admin/revenue-cycle-monitoring")
    public String revenueCycleMonitoring(
            @RequestParam(value = "range", required = false, defaultValue = "today") String range,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "insuranceCode", required = false) String insuranceCode,
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        AdminRevenueCycleService.MonitoringData data = adminRevenueCycleService.loadMonitoring(
                range, departmentCode, doctorId, insuranceCode, status);
        model.addAttribute("kanbanColumns", data.kanbanColumns());
        model.addAttribute("visitRows", data.visitRows());
        model.addAttribute("selectedVisit", data.selectedVisit());
        model.addAttribute("departmentOptions", adminDashboardService.getDepartmentOptions());
        model.addAttribute("doctorOptions", adminDashboardService.getDoctorOptions());
        model.addAttribute("insuranceOptions", adminDashboardService.getInsuranceOptions());
        model.addAttribute("selectedRange", range);
        model.addAttribute("selectedDepartmentCode", departmentCode);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedInsuranceCode", insuranceCode);
        model.addAttribute("selectedStatus", status);
        return "admin/admin-revenue-cycle-monitoring";
    }

    @GetMapping("/admin/inventory-monitoring")
    public String inventoryMonitoring(
            @RequestParam(value = "itemType", required = false) String itemType,
            @RequestParam(value = "warehouse", required = false) String warehouse,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {
        AdminInventoryService.InventoryMonitoringData data = adminInventoryService.loadInventoryMonitoring(
                itemType, warehouse, status, startDate, endDate);
        model.addAttribute("inventoryRows", data.inventoryRows());
        model.addAttribute("lotRows", data.lotRows());
        model.addAttribute("moveRows", data.moveRows());
        model.addAttribute("lockinRows", data.lockinRows());
        model.addAttribute("warehouseOptions", data.warehouseOptions());
        model.addAttribute("selectedItem", data.selectedRow());
        model.addAttribute("selectedItemType", itemType);
        model.addAttribute("selectedWarehouse", warehouse);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        return "admin/admin-inventory-monitoring";
    }

    @GetMapping("/admin/sales-report")
    public String salesReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "insuranceCode", required = false) String insuranceCode,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "visitType", required = false) String visitType,
            Model model) {
        AdminReportService.SalesReportData data = adminReportService.loadSalesReport(
                startDate, endDate, departmentCode, doctorId, insuranceCode, paymentMethod, visitType);
        long dailyMaxTotal = data.dailyRows().stream()
                .mapToLong(AdminReportService.DailySalesRow::totalAmount)
                .max()
                .orElse(0);
        model.addAttribute("summary", data.summary());
        model.addAttribute("dailyRows", data.dailyRows());
        model.addAttribute("dailyMaxTotal", dailyMaxTotal);
        model.addAttribute("paymentRows", data.paymentMethods());
        model.addAttribute("rankRows", data.rankRows());
        model.addAttribute("paymentMethodOptions", data.paymentMethodOptions());
        model.addAttribute("departmentOptions", adminDashboardService.getDepartmentOptions());
        model.addAttribute("doctorOptions", adminDashboardService.getDoctorOptions());
        model.addAttribute("insuranceOptions", adminDashboardService.getInsuranceOptions());
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        model.addAttribute("selectedDepartmentCode", departmentCode);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedInsuranceCode", insuranceCode);
        model.addAttribute("selectedPaymentMethod", paymentMethod);
        model.addAttribute("selectedVisitType", visitType);
        return "admin/admin-sales-report";
    }

    @GetMapping("/admin/insurance-sales")
    public String insuranceSales(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "doctorId", required = false) String doctorId,
            @RequestParam(value = "insuranceCode", required = false) String insuranceCode,
            Model model) {
        AdminReportService.InsuranceSalesData data = adminReportService.loadInsuranceSales(
                startDate, endDate, departmentCode, doctorId, insuranceCode);
        long insuranceMaxTotal = data.rows().stream()
                .mapToLong(AdminReportService.InsuranceRow::amount)
                .max()
                .orElse(0);
        model.addAttribute("rows", data.rows());
        model.addAttribute("insuranceMaxTotal", insuranceMaxTotal);
        model.addAttribute("departmentOptions", adminDashboardService.getDepartmentOptions());
        model.addAttribute("doctorOptions", adminDashboardService.getDoctorOptions());
        model.addAttribute("insuranceOptions", adminDashboardService.getInsuranceOptions());
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        model.addAttribute("selectedDepartmentCode", departmentCode);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedInsuranceCode", insuranceCode);
        return "admin/admin-insurance-sales";
    }

    @GetMapping("/admin/receivables-report")
    public String receivablesReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "receivableStatus", required = false) String receivableStatus,
            @RequestParam(value = "postpaid", required = false) String postpaid,
            @RequestParam(value = "insuranceCode", required = false) String insuranceCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        AdminReceivableService.ReceivableData data = adminReceivableService.loadReceivables(
                startDate, endDate, receivableStatus, postpaid, insuranceCode, keyword);
        model.addAttribute("rows", data.rows());
        model.addAttribute("selected", data.selected());
        model.addAttribute("insuranceOptions", adminDashboardService.getInsuranceOptions());
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);
        model.addAttribute("selectedReceivableStatus", receivableStatus);
        model.addAttribute("selectedInsuranceCode", insuranceCode);
        model.addAttribute("selectedKeyword", keyword);
        return "admin/admin-receivables-report";
    }

    @GetMapping("/admin/purchase-approval")
    public String purchaseApproval(Model model) {
        model.addAttribute("rows", adminApprovalService.loadPurchaseApprovals());
        return "admin/admin-purchase-approval";
    }

    @GetMapping("/admin/price-exception-approval")
    public String priceExceptionApproval(Model model) {
        model.addAttribute("rows", adminApprovalService.loadPriceExceptionApprovals());
        return "admin/admin-price-exception-approval";
    }

    @GetMapping("/admin/item-approval")
    public String itemApproval(Model model) {
        model.addAttribute("rows", adminApprovalService.loadNewItemApprovals());
        return "admin/admin-item-approval";
    }

    @GetMapping("/admin/stock-lockin")
    public String stockLockin(Model model) {
        model.addAttribute("rows", adminApprovalService.loadStockLockins());
        return "admin/admin-stock-lockin";
    }

    @GetMapping("/admin/user-management")
    public String userManagement(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        AdminUserService.UserData data = adminUserService.loadUsers(keyword, department, role, status);
        model.addAttribute("rows", data.rows());
        model.addAttribute("departmentOptions", adminDashboardService.getDepartmentOptions());
        model.addAttribute("roleOptions", adminUserService.loadRoleOptions());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        return "admin/admin-user-management";
    }

    @GetMapping("/admin/role-permission")
    public String rolePermission(Model model) {
        model.addAttribute("rows", adminCodeService.loadRoles());
        return "admin/admin-role-permission";
    }

    @GetMapping("/admin/code-department")
    public String codeDepartment(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) String active,
            Model model) {
        model.addAttribute("rows", adminCodeService.loadDepartmentCodes(keyword, active));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedActive", active);
        return "admin/admin-code-department";
    }

    @GetMapping("/admin/code-insurance")
    public String codeInsurance(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) String active,
            Model model) {
        model.addAttribute("rows", adminCodeService.loadInsuranceCodes(keyword, active));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedActive", active);
        return "admin/admin-code-insurance";
    }

    @GetMapping("/admin/code-payment-method")
    public String codePaymentMethod(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) String active,
            Model model) {
        model.addAttribute("rows", adminCodeService.loadPaymentMethods(keyword, active));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedActive", active);
        return "admin/admin-code-payment-method";
    }

    @GetMapping("/admin/code-fee-item")
    public String codeFeeItem(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) String active,
            Model model) {
        model.addAttribute("rows", adminCodeService.loadFeeItems(keyword, active));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedActive", active);
        return "admin/admin-code-fee-item";
    }

    @GetMapping("/admin/code-diagnosis")
    public String codeDiagnosis(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) String active,
            Model model) {
        model.addAttribute("rows", adminCodeService.loadDiseases(keyword, active));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedActive", active);
        return "admin/admin-code-diagnosis";
    }

    @GetMapping("/admin/code-status")
    public String codeStatus(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "active", required = false) String active,
            Model model) {
        model.addAttribute("rows", adminCodeService.loadStatusCodes(keyword, active));
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedActive", active);
        return "admin/admin-code-status";
    }

    @GetMapping("/admin/work-status")
    public String workStatus(
            @RequestParam(value = "workDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workDate,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "workTypeCode", required = false) String workTypeCode,
            Model model) {
        model.addAttribute("rows", adminWorkService.loadWorkStatus(workDate, departmentCode, workTypeCode));
        model.addAttribute("departmentOptions", adminDashboardService.getDepartmentOptions());
        model.addAttribute("workTypeOptions", adminWorkService.loadWorkTypes());
        model.addAttribute("selectedWorkDate", workDate);
        model.addAttribute("selectedDepartmentCode", departmentCode);
        model.addAttribute("selectedWorkTypeCode", workTypeCode);
        return "admin/admin-work-status";
    }

    @GetMapping("/admin/work-type")
    public String workType(Model model) {
        model.addAttribute("rows", adminWorkService.loadWorkTypes());
        return "admin/admin-work-type";
    }
}
