package com.database.societyproject.generalcomplaint.repository;

import com.database.societyproject.generalcomplaint.entity.GeneralComplaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneralComplaintRepository extends JpaRepository<GeneralComplaint, Long> {
    List<GeneralComplaint> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
    List<GeneralComplaint> findByRaisedByUserIdOrderByCreatedAtDesc(Long userId);
}
