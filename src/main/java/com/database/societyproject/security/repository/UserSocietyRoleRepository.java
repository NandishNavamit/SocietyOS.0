package com.database.societyproject.security.repository;

import com.database.societyproject.security.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSocietyRoleRepository extends JpaRepository<UserSocietyRole, Long> {
    List<UserSocietyRole> findByUserIdAndActiveTrue(Long userId);
    boolean existsByUserIdAndRoleAndActiveTrue(Long userId, UserRole role);
    boolean existsByUserIdAndSocietyIdAndRoleAndActiveTrue(Long userId, Long societyId, UserRole role);
    boolean existsByUserIdAndSocietyIdAndRole(Long userId, Long societyId, UserRole role);
    boolean existsByUserIdAndSocietyIdAndActiveTrue(Long userId, Long societyId);
    Optional<UserSocietyRole> findByUserIdAndSocietyIdAndRole(Long userId, Long societyId, UserRole role);
    Optional<UserSocietyRole> findByUserIdAndSocietyIsNullAndRole(Long userId, UserRole role);
    List<UserSocietyRole> findBySocietyIdAndActiveTrue(Long societyId);
    List<UserSocietyRole> findBySocietyIdAndRoleAndActiveTrue(Long societyId, UserRole role);
}
