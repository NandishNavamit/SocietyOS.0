package com.database.societyproject.onboarding.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.notification.service.EmailDeliveryService;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.onboarding.dto.*;
import com.database.societyproject.onboarding.entity.*;
import com.database.societyproject.onboarding.repository.SocietyInvitationRepository;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import com.database.societyproject.occupancy.entity.FlatOccupancy;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.security.entity.*;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class SocietyInvitationService {
    private final SocietyInvitationRepository invitations; private final SocietyService societies; private final UserRepository users;
    private final UserSocietyRoleRepository roles; private final SocietyAuthorizationService authorization; private final ResidentService residents;
    private final FlatRepository flats; private final FlatOccupancyRepository occupancies;
    private final EmailDeliveryService email; private final String frontendUrl;
    private final NotificationService notifications;
    public SocietyInvitationService(SocietyInvitationRepository invitations, SocietyService societies, UserRepository users, UserSocietyRoleRepository roles, SocietyAuthorizationService authorization, ResidentService residents, FlatRepository flats, FlatOccupancyRepository occupancies, EmailDeliveryService email, NotificationService notifications, @Value("${app.frontend.url}") String frontendUrl) { this.invitations=invitations; this.societies=societies; this.users=users; this.roles=roles; this.authorization=authorization; this.residents=residents; this.flats=flats; this.occupancies=occupancies; this.email=email; this.notifications=notifications; this.frontendUrl=frontendUrl.replaceAll("/$", ""); }
    public SocietyInvitationResponse create(SocietyInvitationRequest request) {
        requireInviteRole(request.role()); authorization.requireManager(request.societyId()); Society society=societies.findEntityForUpdate(request.societyId());
        String address=request.email().trim().toLowerCase(Locale.ROOT);
        Flat flat = ownerFlat(request, society);
        boolean duplicate = request.role() == UserRole.FLAT_OWNER
                ? invitations.existsBySocietyIdAndEmailIgnoreCaseAndInvitedRoleAndFlatIdAndStatus(society.getId(), address, request.role(), flat.getId(), SocietyInvitationStatus.PENDING)
                : invitations.existsBySocietyIdAndEmailIgnoreCaseAndInvitedRoleAndStatus(society.getId(), address, request.role(), SocietyInvitationStatus.PENDING);
        if (duplicate) throw new DuplicateResourceException("A matching invitation is already pending for this assignment");
        String rawToken=UUID.randomUUID()+"-"+UUID.randomUUID(); SocietyInvitation invite=new SocietyInvitation(); invite.setSociety(society); invite.setEmail(address); invite.setInvitedRole(request.role()); invite.setFlat(flat); invite.setTokenHash(hash(rawToken)); invite.setInvitedBy(users.getReferenceById(authorization.currentUserId())); invite.setExpiresAt(LocalDateTime.now().plusDays(7)); SocietyInvitation saved=invitations.save(invite); String url=url(rawToken);
        email.sendIfEnabled(address, "You are invited to " + society.getName() + " on SocietyOS", "You have been invited as " + request.role().name().replace('_', ' ') + ".\n\nOpen this link to sign in or create your account and accept the invitation:\n" + url + "\n\nThis invitation expires in 7 days.");
        return response(saved, url);
    }
    @Transactional(readOnly=true) public List<SocietyInvitationResponse> bySociety(Long societyId) { authorization.requireManager(societyId); return invitations.findBySocietyIdOrderByCreatedAtDesc(societyId).stream().map(i->response(i,null)).toList(); }
    @Transactional(readOnly=true) public InvitationPreviewResponse preview(String token) { SocietyInvitation invitation=findToken(token); expireIfNeeded(invitation); return new InvitationPreviewResponse(invitation.getSociety().getName(), invitation.getInvitedRole(), invitation.getStatus(), invitation.getExpiresAt(), emailHint(invitation.getEmail()), invitation.getFlat() == null ? null : invitation.getFlat().getFlatNumber()); }
    public SocietyInvitationResponse accept(String token) { SocietyInvitation invitation=findToken(token); expireIfNeeded(invitation); if(invitation.getStatus()!=SocietyInvitationStatus.PENDING) throw new InvalidRequestException("This invitation is no longer available"); User user=users.getReferenceById(authorization.currentUserId()); if(!user.getEmail().equalsIgnoreCase(invitation.getEmail())) throw new AccessDeniedException("Sign in with the email address that received this invitation"); if(invitation.getInvitedRole()==UserRole.FLAT_OWNER) assignOwnerHome(invitation, user); UserSocietyRole role=roles.findByUserIdAndSocietyIdAndRole(user.getId(), invitation.getSociety().getId(), invitation.getInvitedRole()).orElseGet(UserSocietyRole::new); role.setUser(user); role.setSociety(invitation.getSociety()); role.setRole(invitation.getInvitedRole()); role.setActive(true); roles.save(role); invitation.setStatus(SocietyInvitationStatus.ACCEPTED); invitation.setAcceptedBy(user); invitation.setAcceptedAt(LocalDateTime.now()); notifications.notifyInvitationAccepted(invitation); return response(invitation,null); }
    public SocietyInvitationResponse resend(Long id) { SocietyInvitation invitation=invitations.findById(id).orElseThrow(()->new ResourceNotFoundException("Invitation",id)); authorization.requireManager(invitation.getSociety().getId()); expireIfNeeded(invitation); if(invitation.getStatus()!=SocietyInvitationStatus.PENDING) throw new InvalidRequestException("Only a pending invitation can be resent"); String rawToken=UUID.randomUUID()+"-"+UUID.randomUUID(); invitation.setTokenHash(hash(rawToken)); invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); String url=url(rawToken); email.sendIfEnabled(invitation.getEmail(), "Reminder: invitation to " + invitation.getSociety().getName() + " on SocietyOS", "You have a pending invitation as " + invitation.getInvitedRole().name().replace('_', ' ') + ".\n\nOpen this link to sign in or create your account and accept the invitation:\n" + url + "\n\nThis refreshed invitation expires in 7 days."); return response(invitation,url); }
    public void revoke(Long id) { SocietyInvitation invitation=invitations.findById(id).orElseThrow(()->new ResourceNotFoundException("Invitation",id)); authorization.requireManager(invitation.getSociety().getId()); if(invitation.getStatus()==SocietyInvitationStatus.PENDING) invitation.setStatus(SocietyInvitationStatus.REVOKED); }
    private SocietyInvitation findToken(String token) { if(token==null||token.isBlank()) throw new InvalidRequestException("Invitation token is required"); return invitations.findByTokenHash(hash(token)).orElseThrow(()->new InvalidRequestException("Invitation is invalid or no longer available")); }
    private void expireIfNeeded(SocietyInvitation invite) { if(invite.getStatus()==SocietyInvitationStatus.PENDING && invite.getExpiresAt().isBefore(LocalDateTime.now())) invite.setStatus(SocietyInvitationStatus.EXPIRED); }
    private void requireInviteRole(UserRole role) { if(role!=UserRole.FLAT_OWNER&&role!=UserRole.LOGBOOK_OPERATOR) throw new InvalidRequestException("Only flat owner or logbook operator invitations can be sent"); }
    private Flat ownerFlat(SocietyInvitationRequest request, Society society) {
        if (request.role() != UserRole.FLAT_OWNER) {
            if (request.flatId() != null) throw new InvalidRequestException("Only a flat owner invitation may be linked to a flat");
            return null;
        }
        if (request.flatId() == null) throw new InvalidRequestException("Choose the flat for this owner invitation");
        Flat flat = flats.findByIdForUpdate(request.flatId()).orElseThrow(() -> new ResourceNotFoundException("Flat", request.flatId()));
        if (!flat.getBlock().getSociety().getId().equals(society.getId())) throw new InvalidRequestException("The flat must belong to this society");
        if (occupancies.existsByFlatIdAndOccupancyTypeAndResidentRoleAndActiveTrue(flat.getId(), OccupancyType.OWNER, ResidentRole.PRIMARY)) throw new DuplicateResourceException("This flat already has an active owner");
        return flat;
    }
    private void assignOwnerHome(SocietyInvitation invitation, User user) {
        if (invitation.getFlat() == null) throw new InvalidRequestException("This owner invitation is missing its flat assignment");
        Flat flat = flats.findByIdForUpdate(invitation.getFlat().getId()).orElseThrow(() -> new ResourceNotFoundException("Flat", invitation.getFlat().getId()));
        if (occupancies.existsByFlatIdAndOccupancyTypeAndResidentRoleAndActiveTrue(flat.getId(), OccupancyType.OWNER, ResidentRole.PRIMARY)) throw new InvalidRequestException("This flat already has an active owner");
        var resident = residents.ensureProfile(user.getId());
        FlatOccupancy occupancy = new FlatOccupancy(); occupancy.setFlat(flat); occupancy.setResident(residents.findEntity(resident.id())); occupancy.setOccupancyType(OccupancyType.OWNER); occupancy.setResidentRole(ResidentRole.PRIMARY); occupancy.setStartDate(LocalDate.now()); occupancy.setActive(true); occupancies.save(occupancy);
    }
    private String hash(String token) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); } catch(Exception exception) { throw new IllegalStateException(exception); } }
    private String emailHint(String email) { int at = email.indexOf('@'); if (at <= 1) return "•••" + (at < 0 ? "" : email.substring(at)); return email.substring(0, 1) + "•••" + email.substring(at); }
    private String url(String rawToken) { return frontendUrl+"/invite?token="+java.net.URLEncoder.encode(rawToken, StandardCharsets.UTF_8); }
    private SocietyInvitationResponse response(SocietyInvitation value,String url) { return new SocietyInvitationResponse(value.getId(),value.getSociety().getId(),value.getSociety().getName(),value.getEmail(),value.getInvitedRole(),value.getFlat()==null?null:value.getFlat().getId(),value.getFlat()==null?null:value.getFlat().getFlatNumber(),value.getStatus(),value.getExpiresAt(),value.getCreatedAt(),url); }
}
