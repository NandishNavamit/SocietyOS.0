package com.database.societyproject.finance.repository;
import com.database.societyproject.finance.entity.ServiceCharge;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServiceChargeRepository extends JpaRepository<ServiceCharge,Long>{ List<ServiceCharge> findBySocietyIdOrderByCreatedAtDesc(Long societyId); boolean existsByComplaintIdAndStatus(Long complaintId, com.database.societyproject.finance.entity.ServiceChargeStatus status); }
