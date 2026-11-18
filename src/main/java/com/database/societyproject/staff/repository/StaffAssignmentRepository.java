package com.database.societyproject.staff.repository;
import com.database.societyproject.staff.entity.StaffAssignment;
import com.database.societyproject.staff.entity.StaffType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
    List<StaffAssignment> findBySocietyId(Long societyId);
    List<StaffAssignment> findBySocietyIdAndStaffTypeAndActiveTrue(Long societyId, StaffType staffType);
    List<StaffAssignment> findByStaffProfileId(Long staffProfileId);
    boolean existsByStaffProfileIdAndSocietyId(Long staffProfileId, Long societyId);
    boolean existsByStaffProfileIdAndSocietyIdAndIdNot(Long staffProfileId, Long societyId, Long id);
    Optional<StaffAssignment> findByStaffProfileUserIdAndSocietyIdAndStaffTypeAndActiveTrue(Long userId, Long societyId, StaffType staffType);
}
