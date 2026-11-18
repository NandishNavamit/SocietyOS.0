package com.database.societyproject.setup.dto;

import java.util.List;

public record SocietySetupResponse(Long societyId, String societyName, List<Block> blocks,
                                   int totalFlats, int ownerReadyFlats, int pendingOwnerInvitations,
                                   int activeLogbookOperators, int pendingOperatorInvitations,
                                   List<Operator> operators, List<Invitation> pendingOperatorInvitationsList) {
    public record Block(Long id, String name, Integer totalFloors, List<Flat> flats) { }
    public record Flat(Long id, String flatNumber, Integer floorNumber, String flatType, Double areaSqft,
                       Owner owner, Invitation pendingInvitation) { }
    public record Owner(Long userId, String fullName, String email) { }
    public record Invitation(Long id, String email, String status, String expiresAt) { }
    public record Operator(Long roleId, Long userId, String fullName, String email) { }
}
