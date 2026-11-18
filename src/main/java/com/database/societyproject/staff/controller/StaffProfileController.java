package com.database.societyproject.staff.controller;
import com.database.societyproject.staff.dto.*;
import com.database.societyproject.staff.service.StaffProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/staff-profiles") @PreAuthorize("denyAll()")
public class StaffProfileController {

    private final StaffProfileService service;
    public StaffProfileController(StaffProfileService service) { this.service = service; }

    @PostMapping public ResponseEntity<StaffProfileResponse> create(@Valid @RequestBody StaffProfileRequest request) {
        return ResponseEntity.status(201).body(service.create(request)); }
    @GetMapping public List<StaffProfileResponse> findAll() {
        return service.findAll(); }
    @GetMapping("/{id}") public StaffProfileResponse findById(@PathVariable Long id) {
        return service.findById(id); }
    @PutMapping("/{id}") public StaffProfileResponse update(@PathVariable Long id, @Valid @RequestBody StaffProfileRequest request) {
        return service.update(id, request); }
}
