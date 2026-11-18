package com.database.societyproject.security.controller;

import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.service.RoleAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class RoleAssignmentController {
    private final RoleAssignmentService service;
    public RoleAssignmentController(RoleAssignmentService service) { this.service = service; }
    @PostMapping("/{userId}/roles") public ResponseEntity<UserRoleAssignmentResponse> assign(@PathVariable Long userId, @Valid @RequestBody UserRoleAssignmentRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.assign(userId, request)); }
    @GetMapping("/{userId}/roles") public List<UserRoleAssignmentResponse> roles(@PathVariable Long userId) { return service.findByUser(userId); }
    @DeleteMapping("/roles/{id}") public UserRoleAssignmentResponse deactivate(@PathVariable Long id) { return service.deactivate(id); }
}
