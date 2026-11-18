package com.database.societyproject.maintenancebilling.entity;

import com.database.societyproject.finance.entity.BillStatus;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.maintenancepolicy.entity.MaintenancePolicyRequest;
import com.database.societyproject.society.entity.Society;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "monthly_maintenance_bills", uniqueConstraints = @UniqueConstraint(name = "uk_monthly_bill_flat_month", columnNames = {"flat_id", "billing_month"}), indexes = @Index(name = "idx_monthly_bill_society", columnList = "society_id,billing_month"))
public class MonthlyMaintenanceBill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "flat_id", nullable = false) private Flat flat;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "policy_request_id", nullable = false) private MaintenancePolicyRequest policyRequest;
    @Column(nullable = false) private LocalDate billingMonth;
    @Column(nullable = false) private LocalDate dueDate;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal maintenanceAmount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal lateFeePerOverdueMonth;
    @Column(nullable = false) private Integer lateFeeMonthsApplied = 0;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal lateFeeAmount = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal totalAmount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal paidAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BillStatus status = BillStatus.PENDING;
    @Column(nullable = false, updatable = false) private LocalDateTime generatedAt;
    @PrePersist void prepare() { if (generatedAt == null) generatedAt = LocalDateTime.now(); if (lateFeeMonthsApplied == null) lateFeeMonthsApplied = 0; if (lateFeeAmount == null) lateFeeAmount = BigDecimal.ZERO; if (paidAmount == null) paidAmount = BigDecimal.ZERO; if (status == null) status = BillStatus.PENDING; }
    public Long getId() { return id; } public Society getSociety() { return society; } public void setSociety(Society value) { society = value; } public Flat getFlat() { return flat; } public void setFlat(Flat value) { flat = value; } public MaintenancePolicyRequest getPolicyRequest() { return policyRequest; } public void setPolicyRequest(MaintenancePolicyRequest value) { policyRequest = value; }
    public LocalDate getBillingMonth() { return billingMonth; } public void setBillingMonth(LocalDate value) { billingMonth = value; } public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate value) { dueDate = value; } public BigDecimal getMaintenanceAmount() { return maintenanceAmount; } public void setMaintenanceAmount(BigDecimal value) { maintenanceAmount = value; } public BigDecimal getLateFeePerOverdueMonth() { return lateFeePerOverdueMonth; } public void setLateFeePerOverdueMonth(BigDecimal value) { lateFeePerOverdueMonth = value; } public Integer getLateFeeMonthsApplied() { return lateFeeMonthsApplied; } public void setLateFeeMonthsApplied(Integer value) { lateFeeMonthsApplied = value; } public BigDecimal getLateFeeAmount() { return lateFeeAmount; } public void setLateFeeAmount(BigDecimal value) { lateFeeAmount = value; } public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal value) { totalAmount = value; } public BigDecimal getPaidAmount() { return paidAmount; } public void setPaidAmount(BigDecimal value) { paidAmount = value; } public BillStatus getStatus() { return status; } public void setStatus(BillStatus value) { status = value; } public LocalDateTime getGeneratedAt() { return generatedAt; }
}
