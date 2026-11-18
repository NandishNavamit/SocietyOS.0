package com.database.societyproject.society.repository;

import com.database.societyproject.society.entity.Society;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SocietyRepository extends JpaRepository<Society, Long> {
    /**
     * Serializes operations, such as invitation creation, that must be unique
     * within one society even when two browser requests arrive together.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select society from Society society where society.id = :id")
    Optional<Society> findByIdForUpdate(Long id);
    List<Society> findByActiveTrue();
}
