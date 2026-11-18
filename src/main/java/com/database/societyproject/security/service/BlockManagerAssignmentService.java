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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlockManagerAssignmentService {
    private final BlockManagerAssignmentRepository repository;
    private final BlockService blocks;
    private final UserService users;
    private final UserSocietyRoleRepository roles;
    private final SocietyAuthorizationService authorization;

    public BlockManagerAssignmentService(BlockManagerAssignmentRepository repository, BlockService blocks,
                                         UserService users, UserSocietyRoleRepository roles,
                                         SocietyAuthorizationService authorization) {
        this.repository = repository; this.blocks = blocks; this.users = users; this.roles = roles; this.authorization = authorization;
    }

    public BlockManagerAssignmentResponse assign(BlockManagerAssignmentRequest request) {
        Block block = blocks.findEntity(request.blockId());
        Long societyId = block.getSociety().getId();
        authorization.requireManager(societyId);
        if (!roles.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(request.userId(), societyId, UserRole.BLOCK_MANAGER)) {
            throw new InvalidRequestException("Assign the Block Manager role before assigning blocks");
        }
        BlockManagerAssignment assignment = repository.findByUserIdAndBlockId(request.userId(), request.blockId()).orElse(null);
        if (assignment != null && Boolean.TRUE.equals(assignment.getActive())) throw new DuplicateResourceException("This manager is already assigned to the selected block");
        if (assignment == null) assignment = new BlockManagerAssignment();
        User user = users.findEntity(request.userId());
        assignment.setUser(user); assignment.setBlock(block); assignment.setActive(true);
        return toResponse(repository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<BlockManagerAssignmentResponse> findBySociety(Long societyId) {
        authorization.requireManager(societyId);
        return repository.findByBlockSocietyIdAndActiveTrue(societyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BlockManagerAssignmentResponse> findMine() {
        return repository.findByUserIdAndActiveTrue(authorization.currentUserId()).stream().map(this::toResponse).toList();
    }

    public BlockManagerAssignmentResponse deactivate(Long id) {
        BlockManagerAssignment assignment = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Block manager assignment", id));
        authorization.requireManager(assignment.getBlock().getSociety().getId());
        assignment.setActive(false);
        return toResponse(assignment);
    }

    private BlockManagerAssignmentResponse toResponse(BlockManagerAssignment assignment) {
        return new BlockManagerAssignmentResponse(assignment.getId(), assignment.getUser().getId(), assignment.getUser().getFullName(),
                assignment.getBlock().getId(), assignment.getBlock().getName(), assignment.getActive(), assignment.getCreatedAt());
    }
}
