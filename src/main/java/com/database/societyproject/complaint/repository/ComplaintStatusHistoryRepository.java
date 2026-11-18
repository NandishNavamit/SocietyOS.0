package com.database.societyproject.complaint.repository;
import com.database.societyproject.complaint.entity.ComplaintStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {
    List<ComplaintStatusHistory> findByComplaintIdOrderByChangedAtAsc(Long complaintId);
}
