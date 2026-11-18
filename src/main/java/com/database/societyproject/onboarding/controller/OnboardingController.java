package com.database.societyproject.onboarding.controller;

import com.database.societyproject.onboarding.dto.*;
import com.database.societyproject.onboarding.service.OnboardingService;
import com.database.societyproject.society.dto.SocietyResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    private final OnboardingService service;
    public OnboardingController(OnboardingService service) { this.service = service; }
    @PostMapping("/society-enquiries") @PreAuthorize("denyAll()") public ResponseEntity<SocietyRegistrationEnquiryResponse> createEnquiry(@Valid @RequestBody SocietyRegistrationEnquiryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createEnquiry(request)); }
    @GetMapping("/society-enquiries") public List<SocietyRegistrationEnquiryResponse> enquiries() { return service.enquiries(); }
    @GetMapping("/societies") public List<SocietyResponse> availableSocieties() { return service.availableSocieties(); }
    @GetMapping("/managed-societies") public List<SocietyResponse> managedSocieties() { return service.managedSocieties(); }
    @GetMapping("/my-societies") public List<SocietyResponse> mySocieties() { return service.mySocieties(); }
    @PostMapping("/membership-requests") @PreAuthorize("denyAll()") public ResponseEntity<MembershipRequestResponse> requestMembership(@Valid @RequestBody MembershipRequestCreate request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.requestMembership(request)); }
    @GetMapping("/membership-requests/mine") @PreAuthorize("denyAll()") public List<MembershipRequestResponse> mine() { return service.mine(); }
    @GetMapping("/membership-requests/society/{societyId}") @PreAuthorize("denyAll()") public List<MembershipRequestResponse> pending(@PathVariable Long societyId) { return service.pending(societyId); }
    @PostMapping("/membership-requests/{id}/approve") @PreAuthorize("denyAll()") public MembershipRequestResponse approve(@PathVariable Long id) { return service.approve(id); }
    @PostMapping("/membership-requests/{id}/reject") @PreAuthorize("denyAll()") public MembershipRequestResponse reject(@PathVariable Long id) { return service.reject(id); }
}
