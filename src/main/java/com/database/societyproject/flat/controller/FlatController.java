package com.database.societyproject.flat.controller;

import com.database.societyproject.flat.dto.FlatRequest;
import com.database.societyproject.flat.dto.FlatResponse;
import com.database.societyproject.flat.service.FlatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flats")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class FlatController {
    private final FlatService service;
    public FlatController(FlatService service) { this.service = service; }
    @PostMapping public ResponseEntity<FlatResponse> create(@Valid @RequestBody FlatRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping public List<FlatResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public FlatResponse findById(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/block/{blockId}") public List<FlatResponse> findByBlockId(@PathVariable Long blockId) { return service.findByBlockId(blockId); }
    @PutMapping("/{id}") public FlatResponse update(@PathVariable Long id, @Valid @RequestBody FlatRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.deactivate(id); return ResponseEntity.noContent().build(); }
}
