package com.database.societyproject.security.service;

import com.database.societyproject.block.entity.Block;
import com.database.societyproject.block.service.BlockService;
import com.database.societyproject.common.exception.*;
import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.entity.*;
import com.database.societyproject.security.repository.*;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.service.UserService;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class BlockAuthorityService {
    private final UserBlockAssignmentRepository assignments;
    private final UserSocietyRoleRepository roles;
    private final UserService users;
    private final BlockService blocks;
    private final SocietyAuthorizationService authorization;
    public BlockAuthorityService(UserBlockAssignmentRepository assignments, UserSocietyRoleRepository roles, UserService users, BlockService blocks, SocietyAuthorizationService authorization) {
        this.assignments = assignments; this.roles = roles; this.users = users; this.blocks = blocks; this.authorization = authorization;
    }
    public UserBlockAssignmentResponse assign(UserBlockAssignmentRequest request) {
        Block block = blocks.findEntity(request.blockId());
        if (!authorization.isAdmin()) authorization.requireManager(block.getSociety().getId());
        User user = users.findEntity(request.userId());
        if (!roles.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(user.getId(), block.getSociety().getId(), UserRole.BLOCK_MANAGER)) {
            throw new InvalidRequestException("User must first have the BLOCK_MANAGER role for this society");
        }
        if (assignments.existsByUserIdAndBlockIdAndActiveTrue(user.getId(), block.getId())) throw new DuplicateResourceException("User is already assigned to this block");
        UserBlockAssignment assignment = new UserBlockAssignment(); assignment.setUser(user); assignment.setBlock(block); assignment.setActive(true);
        return response(assignments.save(assignment));
    }
    @Transactional(readOnly = true) public List<UserBlockAssignmentResponse> byUser(Long userId) {
        if (!authorization.isAdmin() && !authorization.currentUserId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own block assignments");
        }
        return assignments.findByUserIdAndActiveTrue(userId).stream().map(this::response).toList();
    }
    public UserBlockAssignmentResponse deactivate(Long id) {
        UserBlockAssignment assignment = assignments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Block assignment", id));
        if (!authorization.isAdmin()) authorization.requireManager(assignment.getBlock().getSociety().getId());
        assignment.setActive(false); return response(assignment);
    }
    private UserBlockAssignmentResponse response(UserBlockAssignment a) { return new UserBlockAssignmentResponse(a.getId(), a.getUser().getId(), a.getBlock().getId(), a.getBlock().getSociety().getId(), a.getActive(), a.getCreatedAt()); }
}
