package com.database.societyproject.setup.service;

import com.database.societyproject.block.dto.BlockRequest;
import com.database.societyproject.block.entity.Block;
import com.database.societyproject.block.repository.BlockRepository;
import com.database.societyproject.block.service.BlockService;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.flat.dto.FlatRequest;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.onboarding.dto.SocietyInvitationRequest;
import com.database.societyproject.onboarding.dto.SocietyInvitationResponse;
import com.database.societyproject.onboarding.entity.SocietyInvitationStatus;
import com.database.societyproject.onboarding.repository.SocietyInvitationRepository;
import com.database.societyproject.onboarding.service.SocietyInvitationService;
import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.setup.dto.*;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SocietySetupService {
    private final SocietyAuthorizationService authorization;
    private final SocietyService societies;
    private final BlockRepository blocks;
    private final FlatRepository flats;
    private final FlatOccupancyRepository occupancies;
    private final SocietyInvitationRepository invitations;
    private final UserSocietyRoleRepository roles;
    private final BlockService blockService;
    private final FlatService flatService;
    private final SocietyInvitationService invitationService;

    public SocietySetupService(SocietyAuthorizationService authorization, SocietyService societies, BlockRepository blocks,
                               FlatRepository flats, FlatOccupancyRepository occupancies, SocietyInvitationRepository invitations,
                               UserSocietyRoleRepository roles, BlockService blockService, FlatService flatService,
                               SocietyInvitationService invitationService) {
        this.authorization = authorization; this.societies = societies; this.blocks = blocks; this.flats = flats;
        this.occupancies = occupancies; this.invitations = invitations; this.roles = roles; this.blockService = blockService;
        this.flatService = flatService; this.invitationService = invitationService;
    }

    @Transactional(readOnly = true)
    public SocietySetupResponse overview(Long societyId) {
        authorization.requireManager(societyId);
        Society society = societies.findEntity(societyId);
        List<com.database.societyproject.onboarding.entity.SocietyInvitation> allInvitations = invitations.findBySocietyIdOrderByCreatedAtDesc(societyId);
        Map<Long, com.database.societyproject.onboarding.entity.SocietyInvitation> pendingByFlat = new HashMap<>();
        for (var invitation : allInvitations) if (invitation.getStatus() == SocietyInvitationStatus.PENDING && invitation.getFlat() != null) pendingByFlat.putIfAbsent(invitation.getFlat().getId(), invitation);
        List<Flat> societyFlats = flats.findByBlockSocietyIdAndActiveTrue(societyId);
        int owners = 0;
        List<SocietySetupResponse.Block> responseBlocks = new ArrayList<>();
        for (Block block : blocks.findBySocietyId(societyId)) {
            List<SocietySetupResponse.Flat> responseFlats = new ArrayList<>();
            for (Flat flat : societyFlats.stream().filter(item -> item.getBlock().getId().equals(block.getId())).toList()) {
                var ownership = occupancies.findByFlatIdAndActiveTrue(flat.getId()).stream().filter(item ->
                        item.getOccupancyType() == OccupancyType.OWNER && item.getResidentRole() == ResidentRole.PRIMARY).findFirst();
                SocietySetupResponse.Owner owner = ownership.map(item -> new SocietySetupResponse.Owner(item.getResident().getUser().getId(), item.getResident().getUser().getFullName(), item.getResident().getUser().getEmail())).orElse(null);
                if (owner != null) owners++;
                var pending = pendingByFlat.get(flat.getId());
                SocietySetupResponse.Invitation pendingResponse = pending == null ? null : new SocietySetupResponse.Invitation(pending.getId(), pending.getEmail(), pending.getStatus().name(), pending.getExpiresAt().toString());
                responseFlats.add(new SocietySetupResponse.Flat(flat.getId(), flat.getFlatNumber(), flat.getFloorNumber(), flat.getFlatType(), flat.getAreaSqft(), owner, pendingResponse));
            }
            responseBlocks.add(new SocietySetupResponse.Block(block.getId(), block.getName(), block.getTotalFloors(), responseFlats));
        }
        int pendingOwners = (int) allInvitations.stream().filter(item -> item.getStatus() == SocietyInvitationStatus.PENDING && item.getInvitedRole() == UserRole.FLAT_OWNER).count();
        int pendingOperators = (int) allInvitations.stream().filter(item -> item.getStatus() == SocietyInvitationStatus.PENDING && item.getInvitedRole() == UserRole.LOGBOOK_OPERATOR).count();
        List<SocietySetupResponse.Operator> activeOperators = roles.findBySocietyIdAndRoleAndActiveTrue(societyId, UserRole.LOGBOOK_OPERATOR).stream()
                .map(item -> new SocietySetupResponse.Operator(item.getId(), item.getUser().getId(), item.getUser().getFullName(), item.getUser().getEmail())).toList();
        List<SocietySetupResponse.Invitation> pendingOperatorList = allInvitations.stream()
                .filter(item -> item.getStatus() == SocietyInvitationStatus.PENDING && item.getInvitedRole() == UserRole.LOGBOOK_OPERATOR)
                .map(item -> new SocietySetupResponse.Invitation(item.getId(), item.getEmail(), item.getStatus().name(), item.getExpiresAt().toString())).toList();
        return new SocietySetupResponse(society.getId(), society.getName(), responseBlocks, societyFlats.size(), owners, pendingOwners, activeOperators.size(), pendingOperators, activeOperators, pendingOperatorList);
    }

    public void addBlock(Long societyId, SetupBlockRequest request) {
        authorization.requireManager(societyId);
        blockService.create(new BlockRequest(request.name(), request.totalFloors(), null, true, societyId));
    }

    public void addFlat(Long blockId, SetupFlatRequest request) {
        Block block = blocks.findById(blockId).orElseThrow(() -> new InvalidRequestException("Block was not found"));
        authorization.requireManager(block.getSociety().getId());
        flatService.create(new FlatRequest(request.flatNumber(), request.floorNumber(), request.flatType(), request.areaSqft(), true, blockId));
    }

    public SocietyInvitationResponse inviteOwner(Long flatId, SetupInviteRequest request) {
        Flat flat = flats.findById(flatId).orElseThrow(() -> new InvalidRequestException("Flat was not found"));
        Long societyId = flat.getBlock().getSociety().getId(); authorization.requireManager(societyId);
        return invitationService.create(new SocietyInvitationRequest(societyId, request.email(), UserRole.FLAT_OWNER, flatId));
    }

    public SocietyInvitationResponse inviteOperator(Long societyId, SetupInviteRequest request) {
        authorization.requireManager(societyId);
        return invitationService.create(new SocietyInvitationRequest(societyId, request.email(), UserRole.LOGBOOK_OPERATOR, null));
    }

    /** Removing access stops an operator opening the private logbook; it never exposes its entries to an administrator. */
    public void revokeOperator(Long societyId, Long roleId) {
        authorization.requireManager(societyId);
        var assignment = roles.findById(roleId).orElseThrow(() -> new InvalidRequestException("Logbook operator assignment was not found"));
        if (assignment.getRole() != UserRole.LOGBOOK_OPERATOR || assignment.getSociety() == null || !assignment.getSociety().getId().equals(societyId)) {
            throw new InvalidRequestException("That operator assignment does not belong to this society");
        }
        assignment.setActive(false);
    }
}
