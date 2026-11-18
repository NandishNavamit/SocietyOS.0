package com.database.societyproject.staff.controller;
import com.database.societyproject.staff.dto.*;
import com.database.societyproject.staff.service.StaffAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/staff-assignments") @PreAuthorize("denyAll()")
public class StaffAssignmentController {
    private final StaffAssignmentService service;
    public StaffAssignmentController(StaffAssignmentService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER')") public ResponseEntity<StaffAssignmentResponse> create(@Valid @RequestBody StaffAssignmentRequest request) { return ResponseEntity.status(201).body(service.create(request)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<StaffAssignmentResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER')") public StaffAssignmentResponse findById(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/society/{societyId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER')") public List<StaffAssignmentResponse> bySociety(@PathVariable Long societyId) { return service.findBySocietyId(societyId); }
    @GetMapping("/society/{societyId}/security-context") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER','SECURITY_MANAGER','SECURITY')") public List<StaffAssignmentResponse> securityContext(@PathVariable Long societyId) { return service.findSecurityContextBySocietyId(societyId); }
    @GetMapping("/profile/{profileId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER','STAFF','SECURITY')") public List<StaffAssignmentResponse> byProfile(@PathVariable Long profileId) { return service.findByProfileId(profileId); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER')") public StaffAssignmentResponse update(@PathVariable Long id, @Valid @RequestBody StaffAssignmentRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_MANAGER')") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
