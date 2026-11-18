package com.database.societyproject.maintenancepayment.repository;
import com.database.societyproject.maintenancepayment.entity.MaintenancePaymentAllocation; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenancePaymentAllocationRepository extends JpaRepository<MaintenancePaymentAllocation,Long>{ List<MaintenancePaymentAllocation> findByPaymentId(Long paymentId); }
