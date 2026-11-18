package com.database.societyproject.flat.repository;

import com.database.societyproject.flat.entity.Flat;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FlatRepository extends JpaRepository<Flat, Long> {
    List<Flat> findByBlockId(Long blockId);
    boolean existsByBlockIdAndFlatNumber(Long blockId, String flatNumber);
    boolean existsByBlockIdAndFlatNumberAndIdNot(Long blockId, String flatNumber, Long id);
    List<Flat> findByBlockSocietyIdAndActiveTrue(Long societyId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select flat from Flat flat where flat.id = :id")
    Optional<Flat> findByIdForUpdate(Long id);
}
