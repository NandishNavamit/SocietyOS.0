package com.database.societyproject.block.controller;

import com.database.societyproject.block.dto.BlockRequest;
import com.database.societyproject.block.dto.BlockResponse;
import com.database.societyproject.block.service.BlockService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class BlockController {

    private final BlockService service;

    public BlockController(BlockService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<BlockResponse> create(@Valid @RequestBody BlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request)); }
    @GetMapping
    public List<BlockResponse> findAll() {
        return service.findAll(); }
    @GetMapping("/{id}")
    public BlockResponse findById(@PathVariable Long id) {
        return service.findById(id); }
    @GetMapping("/society/{societyId}")
    public List<BlockResponse> findBySocietyId(@PathVariable Long societyId) {
        return service.findBySocietyId(societyId); }
    @PutMapping("/{id}")
    public BlockResponse update(@PathVariable Long id, @Valid @RequestBody BlockRequest request) {
        return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
