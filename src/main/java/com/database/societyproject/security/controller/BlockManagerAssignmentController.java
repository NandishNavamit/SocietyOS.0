package com.database.societyproject.security.controller;

import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.service.BlockManagerAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/block-manager-assignments")
@PreAuthorize("denyAll()")
public class BlockManagerAssignmentController {
    private final BlockManagerAssignmentService service;
    public BlockManagerAssignmentController(BlockManagerAssignmentService service) { this.service = service; }

    @PostMapping public ResponseEntity<BlockManagerAssignmentResponse> assign(@Valid @RequestBody BlockManagerAssignmentRequest request) {
        return ResponseEntity.status(201).body(service.assign(request));
    }
    @GetMapping("/mine") public List<BlockManagerAssignmentResponse> mine() { return service.findMine(); }
    @GetMapping("/society/{societyId}") public List<BlockManagerAssignmentResponse> bySociety(@PathVariable Long societyId) { return service.findBySociety(societyId); }
    @DeleteMapping("/{id}") public BlockManagerAssignmentResponse deactivate(@PathVariable Long id) { return service.deactivate(id); }
}
