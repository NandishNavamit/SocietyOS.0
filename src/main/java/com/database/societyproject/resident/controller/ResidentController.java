package com.database.societyproject.resident.controller;

import com.database.societyproject.resident.dto.ResidentRequest;
import com.database.societyproject.resident.dto.ResidentResponse;
import com.database.societyproject.resident.service.ResidentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/residents")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class ResidentController {
    private final ResidentService service;
    public ResidentController(ResidentService service) { this.service = service; }
    @PostMapping public ResponseEntity<ResidentResponse> create(@Valid @RequestBody ResidentRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping public List<ResidentResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public ResidentResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public ResidentResponse update(@PathVariable Long id, @Valid @RequestBody ResidentRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
