package com.database.societyproject.maintenancebilling.controller;

import com.database.societyproject.maintenancebilling.dto.*;
import com.database.societyproject.maintenancebilling.service.MonthlyMaintenanceBillingService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monthly-maintenance")
public class MonthlyMaintenanceBillingController {
    private final MonthlyMaintenanceBillingService service;
    public MonthlyMaintenanceBillingController(MonthlyMaintenanceBillingService service) { this.service = service; }
    @GetMapping("/current-month") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public BillingMonthResponse currentMonth() { return service.currentBillingMonth(); }
    @PostMapping("/societies/{societyId}/generate") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public MonthlyBillGenerationResponse generate(@PathVariable Long societyId, @Valid @RequestBody MonthlyBillGenerationRequest request) { return service.generate(societyId, request); }
    @GetMapping("/societies/{societyId}/bills") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public List<MonthlyMaintenanceBillResponse> bySociety(@PathVariable Long societyId) { return service.bySociety(societyId); }
    @GetMapping("/my-bills") @PreAuthorize("hasRole('FLAT_OWNER')") public List<MonthlyMaintenanceBillResponse> mine() { return service.mine(); }
    @GetMapping("/my-flats") @PreAuthorize("hasRole('FLAT_OWNER')") public List<OwnedFlatResponse> myFlats() { return service.myFlats(); }
}
