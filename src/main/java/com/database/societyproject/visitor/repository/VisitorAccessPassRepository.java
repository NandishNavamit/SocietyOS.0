package com.database.societyproject.visitor.repository;
import com.database.societyproject.visitor.entity.VisitorAccessPass;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VisitorAccessPassRepository extends JpaRepository<VisitorAccessPass, Long> { List<VisitorAccessPass> findByVisitorId(Long visitorId); List<VisitorAccessPass> findBySocietyId(Long societyId); }
