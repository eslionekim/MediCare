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
}
