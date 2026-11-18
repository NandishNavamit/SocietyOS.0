package com.database.societyproject.security.repository;

import com.database.societyproject.security.entity.UserBlockAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBlockAssignmentRepository extends JpaRepository<UserBlockAssignment, Long> {
    List<UserBlockAssignment> findByUserIdAndActiveTrue(Long userId);
    boolean existsByUserIdAndBlockIdAndActiveTrue(Long userId, Long blockId);
}
