package com.database.societyproject.security.controller;

import com.database.societyproject.security.dto.SocietyMemberResponse;
import com.database.societyproject.security.dto.UserRoleAssignmentResponse;
import com.database.societyproject.security.service.RoleAssignmentService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society-roles")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class SocietyRoleController {
    private final RoleAssignmentService service;

    public SocietyRoleController(RoleAssignmentService service) { this.service = service; }

    @GetMapping("/societies/{societyId}/members")
    public List<SocietyMemberResponse> members(@PathVariable Long societyId) {
        return service.findMembersBySociety(societyId);
    }

    @GetMapping("/societies/{societyId}")
    public List<UserRoleAssignmentResponse> roles(@PathVariable Long societyId) {
        return service.findBySociety(societyId);
    }
}
