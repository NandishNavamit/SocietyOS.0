package com.database.societyproject.generalcomplaint.controller;

import com.database.societyproject.generalcomplaint.dto.*;
import com.database.societyproject.generalcomplaint.service.GeneralComplaintService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/general-complaints")
public class GeneralComplaintController {
    private final GeneralComplaintService service;
    public GeneralComplaintController(GeneralComplaintService service) { this.service = service; }
    @PostMapping @PreAuthorize("hasRole('FLAT_OWNER')") public GeneralComplaintResponse create(@Valid @RequestBody GeneralComplaintCreateRequest request) { return service.create(request); }
    @GetMapping("/mine") @PreAuthorize("hasRole('FLAT_OWNER')") public List<GeneralComplaintResponse> mine() { return service.mine(); }
    @GetMapping("/societies/{societyId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public List<GeneralComplaintResponse> bySociety(@PathVariable Long societyId) { return service.bySociety(societyId); }
    @PatchMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public GeneralComplaintResponse update(@PathVariable Long id, @Valid @RequestBody GeneralComplaintUpdateRequest request) { return service.update(id, request); }
}
