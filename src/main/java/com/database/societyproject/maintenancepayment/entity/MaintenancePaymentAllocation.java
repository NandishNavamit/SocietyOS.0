package com.database.societyproject.maintenancepayment.entity;

import com.database.societyproject.maintenancebilling.entity.MonthlyMaintenanceBill;
import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name="monthly_maintenance_payment_allocations", uniqueConstraints=@UniqueConstraint(name="uk_monthly_payment_bill",columnNames={"payment_id","bill_id"}))
public class MaintenancePaymentAllocation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="payment_id",nullable=false) private MaintenancePayment payment;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="bill_id",nullable=false) private MonthlyMaintenanceBill bill;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 public Long getId(){return id;} public MaintenancePayment getPayment(){return payment;} public void setPayment(MaintenancePayment v){payment=v;} public MonthlyMaintenanceBill getBill(){return bill;} public void setBill(MonthlyMaintenanceBill v){bill=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
}
