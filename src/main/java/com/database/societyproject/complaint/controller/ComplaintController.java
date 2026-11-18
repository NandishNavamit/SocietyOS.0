package com.database.societyproject.complaint.controller;

import com.database.societyproject.complaint.dto.*;
import com.database.societyproject.complaint.entity.ComplaintStatus;
import com.database.societyproject.complaint.service.ComplaintService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController @RequestMapping("/api/complaints") @PreAuthorize("denyAll()")
public class ComplaintController {
    private final ComplaintService service;
    public ComplaintController(ComplaintService service) { this.service = service; }
    @PostMapping public ResponseEntity<ComplaintResponse> create(@Valid @RequestBody ComplaintRequest request) { return ResponseEntity.status(201).body(service.create(request)); }
    @GetMapping public List<ComplaintResponse> all() { return service.findAll(); }
    @GetMapping("/{id}") public ComplaintResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/society/{societyId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public List<ComplaintResponse> society(@PathVariable Long societyId) { return service.findBySocietyId(societyId); }
    @GetMapping("/block/{blockId}") public List<ComplaintResponse> block(@PathVariable Long blockId) { return service.findByBlockId(blockId); }
    @GetMapping("/resident/{residentId}") public List<ComplaintResponse> resident(@PathVariable Long residentId) { return service.findByResidentId(residentId); }
    @GetMapping("/status/{status}") public List<ComplaintResponse> status(@PathVariable ComplaintStatus status) { return service.findByStatus(status); }
    @PutMapping("/{id}/assign") public ComplaintResponse assign(@PathVariable Long id, @Valid @RequestBody ComplaintAssignmentRequest request) { return service.assign(id, request); }
    @PutMapping("/{id}/status") public ComplaintResponse status(@PathVariable Long id, @Valid @RequestBody ComplaintStatusUpdateRequest request) { return service.updateStatus(id, request); }
    @GetMapping("/{id}/history") public List<ComplaintStatusHistoryResponse> history(@PathVariable Long id) { return service.history(id); }
}
