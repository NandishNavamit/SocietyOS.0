package com.database.societyproject.finance.controller;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.service.MaintenanceBillService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/maintenance-bills") @PreAuthorize("denyAll()") public class MaintenanceBillController {
    private final MaintenanceBillService service; public MaintenanceBillController(MaintenanceBillService service) { this.service = service; }
    @PostMapping public ResponseEntity<MaintenanceBillResponse> create(@Valid @RequestBody MaintenanceBillRequest r) { return ResponseEntity.status(201).body(service.create(r)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<MaintenanceBillResponse> all() { return service.findAll(); } @GetMapping("/{id}") public MaintenanceBillResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/flat/{flatId}") public List<MaintenanceBillResponse> flat(@PathVariable Long flatId) { return service.findByFlatId(flatId); }
    @GetMapping("/society/{societyId}") public List<MaintenanceBillResponse> society(@PathVariable Long societyId) { return service.findBySocietyId(societyId); }
}
