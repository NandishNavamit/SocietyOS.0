package com.database.societyproject.complaint.repository;
import com.database.societyproject.complaint.entity.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findBySocietyId(Long societyId);
    List<Complaint> findByFlatBlockId(Long blockId);
    List<Complaint> findByAssignedStaffAssignmentId(Long staffAssignmentId);
    List<Complaint> findByRaisedByResidentId(Long residentId);
    List<Complaint> findByStatus(ComplaintStatus status);
}
