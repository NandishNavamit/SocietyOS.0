package com.database.societyproject.setup.controller;

import com.database.societyproject.onboarding.dto.SocietyInvitationResponse;
import com.database.societyproject.setup.dto.*;
import com.database.societyproject.setup.service.SocietySetupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/society-setup")
@PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
public class SocietySetupController {
    private final SocietySetupService service;
    public SocietySetupController(SocietySetupService service) { this.service = service; }
    @GetMapping("/societies/{societyId}") public SocietySetupResponse overview(@PathVariable Long societyId) { return service.overview(societyId); }
    @PostMapping("/societies/{societyId}/blocks") @ResponseStatus(HttpStatus.CREATED) public void addBlock(@PathVariable Long societyId, @Valid @RequestBody SetupBlockRequest request) { service.addBlock(societyId, request); }
    @PostMapping("/blocks/{blockId}/flats") @ResponseStatus(HttpStatus.CREATED) public void addFlat(@PathVariable Long blockId, @Valid @RequestBody SetupFlatRequest request) { service.addFlat(blockId, request); }
    @PostMapping("/flats/{flatId}/owner-invitations") @ResponseStatus(HttpStatus.CREATED) public SocietyInvitationResponse inviteOwner(@PathVariable Long flatId, @Valid @RequestBody SetupInviteRequest request) { return service.inviteOwner(flatId, request); }
    @PostMapping("/societies/{societyId}/operator-invitations") @ResponseStatus(HttpStatus.CREATED) public SocietyInvitationResponse inviteOperator(@PathVariable Long societyId, @Valid @RequestBody SetupInviteRequest request) { return service.inviteOperator(societyId, request); }
    @DeleteMapping("/societies/{societyId}/operator-assignments/{roleId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void revokeOperator(@PathVariable Long societyId, @PathVariable Long roleId) { service.revokeOperator(societyId, roleId); }
}
