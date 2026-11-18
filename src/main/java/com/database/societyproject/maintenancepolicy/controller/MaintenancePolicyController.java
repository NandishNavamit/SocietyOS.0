package com.database.societyproject.maintenancepolicy.controller;

import com.database.societyproject.maintenancepolicy.dto.*;
import com.database.societyproject.maintenancepolicy.service.MaintenancePolicyService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance-policy-requests")
public class MaintenancePolicyController {
    private final MaintenancePolicyService service;
    public MaintenancePolicyController(MaintenancePolicyService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasRole('SOCIETY_ADMIN')") public ResponseEntity<MaintenancePolicyResponse> submit(@Valid @RequestBody MaintenancePolicyRequestCreate request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(request)); }
    @GetMapping("/society/{societyId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public List<MaintenancePolicyResponse> bySociety(@PathVariable Long societyId) { return service.bySociety(societyId); }
    @GetMapping("/pending") @PreAuthorize("hasRole('ADMIN')") public List<MaintenancePolicyResponse> pending() { return service.pending(); }
    @PostMapping("/{id}/approve") @PreAuthorize("hasRole('ADMIN')") public MaintenancePolicyResponse approve(@PathVariable Long id, @Valid @RequestBody MaintenancePolicyReviewRequest request) { return service.approve(id, request); }
    @PostMapping("/{id}/reject") @PreAuthorize("hasRole('ADMIN')") public MaintenancePolicyResponse reject(@PathVariable Long id, @Valid @RequestBody MaintenancePolicyReviewRequest request) { return service.reject(id, request); }
}
