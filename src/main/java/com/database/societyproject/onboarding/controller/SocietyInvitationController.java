package com.database.societyproject.onboarding.controller;
import com.database.societyproject.onboarding.dto.*;
import com.database.societyproject.onboarding.service.SocietyInvitationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/invitations") public class SocietyInvitationController {
 private final SocietyInvitationService service; public SocietyInvitationController(SocietyInvitationService service){this.service=service;}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public ResponseEntity<SocietyInvitationResponse> create(@Valid @RequestBody SocietyInvitationRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
 @GetMapping("/society/{societyId}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public List<SocietyInvitationResponse> society(@PathVariable Long societyId){return service.bySociety(societyId);}
 @GetMapping("/preview") public InvitationPreviewResponse preview(@RequestParam String token){return service.preview(token);}
 @PostMapping("/accept") public SocietyInvitationResponse accept(@RequestParam String token){return service.accept(token);}
 @PostMapping("/{id}/resend") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") public SocietyInvitationResponse resend(@PathVariable Long id){return service.resend(id);}
 @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void revoke(@PathVariable Long id){service.revoke(id);}
}
