package com.database.societyproject.finance.repository;
import com.database.societyproject.finance.entity.Payment;
import com.database.societyproject.finance.entity.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillId(Long billId);
    List<Payment> findByBillFlatBlockSocietyId(Long societyId);
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.bill.id = :billId and p.status = :status")
    BigDecimal totalByBillIdAndStatus(@Param("billId") Long billId, @Param("status") PaymentStatus status);
}
