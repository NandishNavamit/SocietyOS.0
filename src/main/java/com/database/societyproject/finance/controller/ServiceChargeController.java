package com.database.societyproject.finance.controller;

import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.service.ServiceChargeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/service-charges") @PreAuthorize("denyAll()")
public class ServiceChargeController {
    private final ServiceChargeService service;
    public ServiceChargeController(ServiceChargeService service) { this.service=service; }
    @PostMapping public ResponseEntity<ServiceChargeResponse> issue(@Valid @RequestBody ServiceChargeRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.issue(request)); }
    @GetMapping("/society/{societyId}") public List<ServiceChargeResponse> society(@PathVariable Long societyId) { return service.bySociety(societyId); }
    @PostMapping("/{id}/void") public ServiceChargeResponse voidCharge(@PathVariable Long id) { return service.voidCharge(id); }
}
