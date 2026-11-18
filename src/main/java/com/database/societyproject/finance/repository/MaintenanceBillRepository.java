package com.database.societyproject.finance.repository;
import com.database.societyproject.finance.entity.MaintenanceBill;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBill, Long> {
    List<MaintenanceBill> findByFlatId(Long flatId); List<MaintenanceBill> findByFlatBlockSocietyId(Long societyId);
    boolean existsByChargePlanIdAndFlatIdAndBillingPeriodStart(Long planId, Long flatId, LocalDate start);
}
