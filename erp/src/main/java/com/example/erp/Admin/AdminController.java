package com.example.erp.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        AdminDashboardService.DashboardData data = adminDashboardService.loadTodayDashboard();
        model.addAttribute("summary", data.summary());
        model.addAttribute("deptStats", data.deptStats());
        model.addAttribute("doctorStats", data.doctorStats());
        model.addAttribute("insuranceStats", data.insuranceStats());
        model.addAttribute("stockAlerts", data.stockAlerts());
        model.addAttribute("pipeline", data.pipelineCounts());
        return "admin/dashboard";
    }
}
