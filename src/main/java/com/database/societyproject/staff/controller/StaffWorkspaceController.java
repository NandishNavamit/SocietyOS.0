package com.database.societyproject.staff.controller;

import com.database.societyproject.staff.dto.StaffWorkspaceResponse;
import com.database.societyproject.staff.service.StaffWorkspaceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff-workspace")
@PreAuthorize("denyAll()")
public class StaffWorkspaceController {
    private final StaffWorkspaceService service;
    public StaffWorkspaceController(StaffWorkspaceService service) { this.service = service; }
    @GetMapping public StaffWorkspaceResponse mine() { return service.mine(); }
}
