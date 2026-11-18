package com.database.societyproject.report.controller;

import com.database.societyproject.report.dto.*;
import com.database.societyproject.report.service.ReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/societies/{societyId}")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public SocietyDashboardResponse dashboard(@PathVariable Long societyId) { return service.dashboard(societyId); }

    @GetMapping("/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public MaintenanceReportResponse maintenance(@PathVariable Long societyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.maintenance(societyId, from, to);
    }

    @GetMapping("/complaints")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public ComplaintReportResponse complaints(@PathVariable Long societyId) { return service.complaints(societyId); }

    @GetMapping("/visitors")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public VisitorReportResponse visitors(@PathVariable Long societyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.visitors(societyId, from, to);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public StaffReportResponse staff(@PathVariable Long societyId) { return service.staff(societyId); }
}
