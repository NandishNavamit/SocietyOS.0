package com.database.societyproject.visitor.controller;
import com.database.societyproject.visitor.dto.*;
import com.database.societyproject.visitor.service.VisitorAccessPassService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/visitor-access-passes") @PreAuthorize("denyAll()") public class VisitorAccessPassController {
    private final VisitorAccessPassService service; public VisitorAccessPassController(VisitorAccessPassService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER','SECURITY_MANAGER')") public ResponseEntity<VisitorAccessPassResponse> create(@Valid @RequestBody VisitorAccessPassRequest r) { return ResponseEntity.status(201).body(service.create(r)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<VisitorAccessPassResponse> all() { return service.findAll(); } @GetMapping("/{id}") public VisitorAccessPassResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/visitor/{visitorId}") @PreAuthorize("hasRole('ADMIN')") public List<VisitorAccessPassResponse> visitor(@PathVariable Long visitorId) { return service.byVisitor(visitorId); } @GetMapping("/society/{societyId}") public List<VisitorAccessPassResponse> society(@PathVariable Long societyId) { return service.bySociety(societyId); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER','SECURITY_MANAGER')") public VisitorAccessPassResponse update(@PathVariable Long id, @Valid @RequestBody VisitorAccessPassRequest r) { return service.update(id, r); }
    @PutMapping("/{id}/approve") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER','SECURITY_MANAGER')") public VisitorAccessPassResponse approve(@PathVariable Long id) { return service.approve(id); } @PutMapping("/{id}/revoke") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER','SECURITY_MANAGER')") public VisitorAccessPassResponse revoke(@PathVariable Long id) { return service.revoke(id); }
}
