package com.database.societyproject.finance.repository;
import com.database.societyproject.finance.entity.ChargePlan;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ChargePlanRepository extends JpaRepository<ChargePlan, Long> { List<ChargePlan> findBySocietyId(Long societyId); Optional<ChargePlan> findBySocietyIdAndName(Long societyId, String name); }
