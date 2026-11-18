package com.database.societyproject.onboarding.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.onboarding.dto.*;
import com.database.societyproject.onboarding.entity.*;
import com.database.societyproject.onboarding.repository.*;
import com.database.societyproject.security.dto.UserRoleAssignmentRequest;
import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.service.*;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.dto.SocietyResponse;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OnboardingService {
    private final SocietyRegistrationEnquiryRepository enquiries;
    private final MembershipRequestRepository memberships;
    private final SocietyService societies;
    private final UserService users;
    private final SocietyAuthorizationService authorization;
    private final RoleAssignmentService roles;
    private final ResidentService residents;
    private final UserSocietyRoleRepository societyRoles;

    public OnboardingService(SocietyRegistrationEnquiryRepository enquiries, MembershipRequestRepository memberships, SocietyService societies, UserService users, SocietyAuthorizationService authorization, RoleAssignmentService roles, ResidentService residents, UserSocietyRoleRepository societyRoles) {
        this.enquiries = enquiries; this.memberships = memberships; this.societies = societies; this.users = users; this.authorization = authorization; this.roles = roles; this.residents = residents; this.societyRoles = societyRoles;
    }
    public SocietyRegistrationEnquiryResponse createEnquiry(SocietyRegistrationEnquiryRequest request) {
        SocietyRegistrationEnquiry enquiry = new SocietyRegistrationEnquiry();
        enquiry.setContactName(request.contactName()); enquiry.setEmail(request.email()); enquiry.setPhoneNumber(request.phoneNumber()); enquiry.setSocietyName(request.societyName());
        enquiry.setCity(request.city()); enquiry.setEstimatedBlocks(request.estimatedBlocks()); enquiry.setEstimatedFlats(request.estimatedFlats()); enquiry.setMessage(request.message());
        return enquiry(enquiries.save(enquiry));
    }
    @Transactional(readOnly = true) public List<SocietyRegistrationEnquiryResponse> enquiries() { authorization.requireAdmin(); return enquiries.findAll().stream().map(this::enquiry).toList(); }
    @Transactional(readOnly = true) public List<SocietyResponse> availableSocieties() { return societies.findAll().stream().filter(response -> Boolean.TRUE.equals(response.active())).toList(); }
    @Transactional(readOnly = true) public List<SocietyResponse> managedSocieties() {
        if (authorization.isAdmin()) return availableSocieties();
        var ids = societyRoles.findByUserIdAndActiveTrue(authorization.currentUserId()).stream()
                .filter(role -> role.getRole() == UserRole.SOCIETY_ADMIN && role.getSociety() != null)
                .map(role -> role.getSociety().getId()).collect(java.util.stream.Collectors.toSet());
        return societies.findAll().stream().filter(response -> Boolean.TRUE.equals(response.active()) && ids.contains(response.id())).toList();
    }
    @Transactional(readOnly = true) public List<SocietyResponse> mySocieties() {
        if (authorization.isAdmin()) return availableSocieties();
        var ids = societyRoles.findByUserIdAndActiveTrue(authorization.currentUserId()).stream()
                .filter(role -> role.getSociety() != null).map(role -> role.getSociety().getId())
                .collect(java.util.stream.Collectors.toSet());
        return societies.findAll().stream().filter(response -> Boolean.TRUE.equals(response.active()) && ids.contains(response.id())).toList();
    }
    public MembershipRequestResponse requestMembership(MembershipRequestCreate request) {
        if (request.requestedRole() != UserRole.FLAT_OWNER && request.requestedRole() != UserRole.LOGBOOK_OPERATOR) throw new InvalidRequestException("Only flat owner or logbook operator access can be requested. Society administrators are appointed by the platform.");
        Long userId = authorization.currentUserId(); Society society = societies.findEntity(request.societyId());
        if (memberships.existsByUserIdAndSocietyIdAndRequestedRoleAndStatus(userId, society.getId(), request.requestedRole(), MembershipRequestStatus.PENDING)) throw new DuplicateResourceException("A matching membership request is already pending");
        MembershipRequest membership = new MembershipRequest(); membership.setUser(users.findEntity(userId)); membership.setSociety(society); membership.setRequestedRole(request.requestedRole()); membership.setNote(request.note());
        return membership(memberships.save(membership));
    }
    @Transactional(readOnly = true) public List<MembershipRequestResponse> mine() { return memberships.findByUserIdOrderByCreatedAtDesc(authorization.currentUserId()).stream().map(this::membership).toList(); }
    @Transactional(readOnly = true) public List<MembershipRequestResponse> pending(Long societyId) { if (!authorization.isAdmin()) authorization.requireManager(societyId); return memberships.findBySocietyIdAndStatus(societyId, MembershipRequestStatus.PENDING).stream().map(this::membership).toList(); }
    public MembershipRequestResponse approve(Long id) {
        MembershipRequest request = find(id); if (!authorization.isAdmin()) authorization.requireManager(request.getSociety().getId());
        roles.assign(request.getUser().getId(), new UserRoleAssignmentRequest(request.getSociety().getId(), request.getRequestedRole()));
        if (request.getRequestedRole() == UserRole.FLAT_OWNER) residents.ensureProfile(request.getUser().getId());
        request.setStatus(MembershipRequestStatus.APPROVED); return membership(request);
    }
    public MembershipRequestResponse reject(Long id) { MembershipRequest request = find(id); if (!authorization.isAdmin()) authorization.requireManager(request.getSociety().getId()); request.setStatus(MembershipRequestStatus.REJECTED); return membership(request); }
    private MembershipRequest find(Long id) { return memberships.findById(id).orElseThrow(() -> new ResourceNotFoundException("Membership request", id)); }
    private SocietyRegistrationEnquiryResponse enquiry(SocietyRegistrationEnquiry value) { return new SocietyRegistrationEnquiryResponse(value.getId(), value.getContactName(), value.getEmail(), value.getSocietyName(), value.getCity(), value.getStatus(), value.getCreatedAt()); }
    private MembershipRequestResponse membership(MembershipRequest value) { return new MembershipRequestResponse(value.getId(), value.getUser().getId(), value.getUser().getFullName(), value.getUser().getEmail(), value.getSociety().getId(), value.getRequestedRole(), value.getStatus(), value.getNote(), value.getCreatedAt()); }
}
