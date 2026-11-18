package com.database.societyproject.logbook.repository;

import com.database.societyproject.logbook.entity.LogbookEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogbookEntryRepository extends JpaRepository<LogbookEntry, Long> {
    List<LogbookEntry> findBySocietyIdOrderByEnteredAtDesc(Long societyId);
}
