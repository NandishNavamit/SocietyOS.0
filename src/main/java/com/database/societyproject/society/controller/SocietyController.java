package com.database.societyproject.society.controller;

import com.database.societyproject.society.dto.SocietyRequest;
import com.database.societyproject.society.dto.SocietyResponse;
import com.database.societyproject.society.service.SocietyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/societies")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class SocietyController {
    private final SocietyService service;
    public SocietyController(SocietyService service) { this.service = service; }
    @PostMapping public ResponseEntity<SocietyResponse> create(@Valid @RequestBody SocietyRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<SocietyResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public SocietyResponse findById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public SocietyResponse update(@PathVariable Long id, @Valid @RequestBody SocietyRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
