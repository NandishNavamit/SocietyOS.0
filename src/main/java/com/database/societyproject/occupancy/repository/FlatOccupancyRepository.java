package com.database.societyproject.occupancy.repository;

import com.database.societyproject.occupancy.entity.FlatOccupancy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlatOccupancyRepository extends JpaRepository<FlatOccupancy, Long> {
    List<FlatOccupancy> findByFlatId(Long flatId);
    List<FlatOccupancy> findByFlatIdAndActiveTrue(Long flatId);
    boolean existsByFlatIdAndOccupancyTypeAndResidentRoleAndActiveTrue(Long flatId,
            com.database.societyproject.occupancy.entity.OccupancyType occupancyType,
            com.database.societyproject.occupancy.entity.ResidentRole residentRole);
    List<FlatOccupancy> findByFlatBlockSocietyIdAndActiveTrue(Long societyId);
    List<FlatOccupancy> findByResidentId(Long residentId);
    List<FlatOccupancy> findByResidentUserId(Long userId);
    boolean existsByResidentUserIdAndFlatBlockSocietyIdAndOccupancyTypeAndResidentRoleAndActiveTrue(
            Long userId, Long societyId,
            com.database.societyproject.occupancy.entity.OccupancyType occupancyType,
            com.database.societyproject.occupancy.entity.ResidentRole residentRole);
    boolean existsByFlatIdAndResidentIdAndActiveTrue(Long flatId, Long residentId);
    boolean existsByFlatIdAndResidentIdAndActiveTrueAndIdNot(Long flatId, Long residentId, Long id);
    boolean existsByResidentIdAndFlatIdAndActiveTrue(Long residentId, Long flatId);
    boolean existsByResidentIdAndFlatBlockSocietyIdAndActiveTrue(Long residentId, Long societyId);
}
