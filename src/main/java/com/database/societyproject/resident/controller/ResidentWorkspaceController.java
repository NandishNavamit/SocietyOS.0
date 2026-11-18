package com.database.societyproject.resident.controller;

import com.database.societyproject.resident.dto.ResidentWorkspaceResponse;
import com.database.societyproject.resident.service.ResidentWorkspaceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resident-workspace")
@PreAuthorize("hasRole('FLAT_OWNER')")
public class ResidentWorkspaceController {
    private final ResidentWorkspaceService service;
    public ResidentWorkspaceController(ResidentWorkspaceService service) { this.service = service; }
    @GetMapping public ResidentWorkspaceResponse mine() { return service.mine(); }
}
