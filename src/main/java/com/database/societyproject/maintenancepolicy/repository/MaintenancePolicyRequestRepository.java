package com.database.societyproject.maintenancepolicy.repository;

import com.database.societyproject.maintenancepolicy.entity.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenancePolicyRequestRepository extends JpaRepository<MaintenancePolicyRequest, Long> {
    List<MaintenancePolicyRequest> findBySocietyIdOrderByEffectiveMonthDescSubmittedAtDesc(Long societyId);
    List<MaintenancePolicyRequest> findByStatusOrderBySubmittedAtAsc(MaintenancePolicyStatus status);
    Optional<MaintenancePolicyRequest> findFirstBySocietyIdAndStatusAndEffectiveMonthLessThanEqualOrderByEffectiveMonthDesc(
            Long societyId, MaintenancePolicyStatus status, LocalDate effectiveMonth);
    boolean existsBySocietyIdAndEffectiveMonthAndStatusIn(Long societyId, LocalDate effectiveMonth, Collection<MaintenancePolicyStatus> statuses);
}
