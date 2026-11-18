package com.database.societyproject.finance.controller;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.service.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/charge-plans") @PreAuthorize("denyAll()") public class ChargePlanController {
    private final ChargePlanService service; private final MaintenanceBillService billService;
    public ChargePlanController(ChargePlanService service, MaintenanceBillService billService) { this.service = service; this.billService = billService; }
    @PostMapping public ResponseEntity<ChargePlanResponse> create(@Valid @RequestBody ChargePlanRequest r) { return ResponseEntity.status(201).body(service.create(r)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<ChargePlanResponse> all() { return service.findAll(); } @GetMapping("/{id}") public ChargePlanResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/society/{societyId}") public List<ChargePlanResponse> society(@PathVariable Long societyId) { return service.findBySocietyId(societyId); }
    @PutMapping("/{id}") public ChargePlanResponse update(@PathVariable Long id, @Valid @RequestBody ChargePlanRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/generate-bills") public List<MaintenanceBillResponse> generate(@PathVariable Long id, @Valid @RequestBody BillGenerationRequest r) { return billService.generate(id, r); }
}
