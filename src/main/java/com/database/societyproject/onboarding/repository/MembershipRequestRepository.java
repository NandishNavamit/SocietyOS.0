package com.database.societyproject.onboarding.repository;

import com.database.societyproject.onboarding.entity.*;
import com.database.societyproject.security.entity.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRequestRepository extends JpaRepository<MembershipRequest, Long> {
    List<MembershipRequest> findBySocietyIdAndStatus(Long societyId, MembershipRequestStatus status);
    List<MembershipRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndSocietyIdAndRequestedRoleAndStatus(Long userId, Long societyId, UserRole role, MembershipRequestStatus status);
}
