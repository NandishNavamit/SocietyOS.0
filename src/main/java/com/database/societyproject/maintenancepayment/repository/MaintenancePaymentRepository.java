package com.database.societyproject.maintenancepayment.repository;
import com.database.societyproject.maintenancepayment.entity.MaintenancePayment; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface MaintenancePaymentRepository extends JpaRepository<MaintenancePayment,Long>{ Optional<MaintenancePayment> findByRazorpayOrderId(String orderId); List<MaintenancePayment> findBySocietyIdOrderByCreatedAtDesc(Long societyId); List<MaintenancePayment> findByPayerIdOrderByCreatedAtDesc(Long payerId); }
