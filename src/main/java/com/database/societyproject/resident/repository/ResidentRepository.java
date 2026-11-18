package com.database.societyproject.resident.repository;

import com.database.societyproject.resident.entity.Resident;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
    boolean existsByUserId(Long userId);
    Optional<Resident> findByUserId(Long userId);
}
