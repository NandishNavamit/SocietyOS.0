package com.database.societyproject.security.repository;

import com.database.societyproject.security.entity.BlockManagerAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockManagerAssignmentRepository extends JpaRepository<BlockManagerAssignment, Long> {
    List<BlockManagerAssignment> findByBlockSocietyIdAndActiveTrue(Long societyId);
    List<BlockManagerAssignment> findByUserIdAndActiveTrue(Long userId);
    Optional<BlockManagerAssignment> findByUserIdAndBlockId(Long userId, Long blockId);
    boolean existsByUserIdAndBlockIdAndActiveTrue(Long userId, Long blockId);
}
