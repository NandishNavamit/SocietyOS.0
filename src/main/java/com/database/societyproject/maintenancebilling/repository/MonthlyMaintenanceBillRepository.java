package com.database.societyproject.maintenancebilling.repository;

import com.database.societyproject.maintenancebilling.entity.MonthlyMaintenanceBill;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface MonthlyMaintenanceBillRepository extends JpaRepository<MonthlyMaintenanceBill, Long> {
    boolean existsByFlatIdAndBillingMonth(Long flatId, LocalDate billingMonth);
    List<MonthlyMaintenanceBill> findBySocietyIdOrderByBillingMonthDescFlatFlatNumberAsc(Long societyId);
    List<MonthlyMaintenanceBill> findByFlatIdInOrderByBillingMonthDesc(Collection<Long> flatIds);
    List<MonthlyMaintenanceBill> findByStatusNotOrderByDueDateAsc(com.database.societyproject.finance.entity.BillStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select bill from MonthlyMaintenanceBill bill where bill.id = :id") Optional<MonthlyMaintenanceBill> findByIdForUpdate(Long id);
}
