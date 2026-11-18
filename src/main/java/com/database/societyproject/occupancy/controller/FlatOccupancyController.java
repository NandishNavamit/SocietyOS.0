package com.database.societyproject.occupancy.controller;

import com.database.societyproject.occupancy.dto.FlatOccupancyRequest;
import com.database.societyproject.occupancy.dto.FlatOccupancyResponse;
import com.database.societyproject.occupancy.service.FlatOccupancyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flat-occupancies")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class FlatOccupancyController {
    private final FlatOccupancyService service;
    public FlatOccupancyController(FlatOccupancyService service) { this.service = service; }
    @PostMapping public ResponseEntity<FlatOccupancyResponse> create(@Valid @RequestBody FlatOccupancyRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping public List<FlatOccupancyResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public FlatOccupancyResponse findById(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/flat/{flatId}") public List<FlatOccupancyResponse> findByFlatId(@PathVariable Long flatId) { return service.findByFlatId(flatId); }
    @GetMapping("/resident/{residentId}") public List<FlatOccupancyResponse> findByResidentId(@PathVariable Long residentId) { return service.findByResidentId(residentId); }
    @PutMapping("/{id}") public FlatOccupancyResponse update(@PathVariable Long id, @Valid @RequestBody FlatOccupancyRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
