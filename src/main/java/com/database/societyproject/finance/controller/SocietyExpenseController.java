package com.database.societyproject.finance.controller;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.service.SocietyExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/society-expenses") @PreAuthorize("denyAll()") public class SocietyExpenseController {
    private final SocietyExpenseService service; public SocietyExpenseController(SocietyExpenseService service) { this.service = service; }
    @PostMapping public ResponseEntity<SocietyExpenseResponse> create(@Valid @RequestBody SocietyExpenseRequest r) { return ResponseEntity.status(201).body(service.create(r)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<SocietyExpenseResponse> all() { return service.findAll(); } @GetMapping("/{id}") public SocietyExpenseResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/society/{societyId}") public List<SocietyExpenseResponse> society(@PathVariable Long societyId) { return service.findBySocietyId(societyId); }
    @PutMapping("/{id}") public SocietyExpenseResponse update(@PathVariable Long id, @Valid @RequestBody SocietyExpenseRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
