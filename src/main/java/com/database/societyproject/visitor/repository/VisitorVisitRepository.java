package com.database.societyproject.visitor.repository;
import com.database.societyproject.visitor.entity.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitorVisitRepository extends JpaRepository<VisitorVisit, Long> { List<VisitorVisit> findBySocietyId(Long societyId); List<VisitorVisit> findBySocietyIdAndEntryTimeAfterOrderByEntryTimeDesc(Long societyId, LocalDateTime after); List<VisitorVisit> findByFlatId(Long flatId); List<VisitorVisit> findByVisitorId(Long visitorId); List<VisitorVisit> findByStatus(VisitStatus status); }
