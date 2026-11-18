package com.database.societyproject.onboarding.repository;
import com.database.societyproject.onboarding.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SocietyInvitationRepository extends JpaRepository<SocietyInvitation, Long> {
    Optional<SocietyInvitation> findByTokenHash(String tokenHash);
    List<SocietyInvitation> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
    boolean existsBySocietyIdAndEmailIgnoreCaseAndInvitedRoleAndStatus(Long societyId, String email, com.database.societyproject.security.entity.UserRole role, SocietyInvitationStatus status);
    boolean existsBySocietyIdAndEmailIgnoreCaseAndInvitedRoleAndFlatIdAndStatus(Long societyId, String email,
            com.database.societyproject.security.entity.UserRole role, Long flatId, SocietyInvitationStatus status);
}
