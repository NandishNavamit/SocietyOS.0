package com.database.societyproject.finance.entity;

import com.database.societyproject.flat.entity.Flat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "maintenance_bills", uniqueConstraints = @UniqueConstraint(name = "uk_bill_plan_flat_period", columnNames = {"charge_plan_id", "flat_id", "billing_period_start"}))
public class MaintenanceBill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "charge_plan_id", nullable = false) private ChargePlan chargePlan;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "flat_id", nullable = false) private Flat flat;
    @Column(nullable = false) private LocalDate billingPeriodStart;
    @Column(nullable = false) private LocalDate billingPeriodEnd;
    @Column(nullable = false) private LocalDate dueDate;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(length = 1000) private String chargeDescription;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal paidAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BillStatus status = BillStatus.PENDING;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (paidAmount == null) paidAmount = BigDecimal.ZERO; if (status == null) status = BillStatus.PENDING; }
    public Long getId() { return id; } public ChargePlan getChargePlan() { return chargePlan; } public void setChargePlan(ChargePlan chargePlan) { this.chargePlan = chargePlan; } public Flat getFlat() { return flat; } public void setFlat(Flat flat) { this.flat = flat; }
    public LocalDate getBillingPeriodStart() { return billingPeriodStart; } public void setBillingPeriodStart(LocalDate value) { billingPeriodStart = value; } public LocalDate getBillingPeriodEnd() { return billingPeriodEnd; } public void setBillingPeriodEnd(LocalDate value) { billingPeriodEnd = value; }
    public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate value) { dueDate = value; } public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal value) { amount = value; }
    public String getChargeDescription() { return chargeDescription; } public void setChargeDescription(String value) { chargeDescription = value; }
    public BigDecimal getPaidAmount() { return paidAmount; } public void setPaidAmount(BigDecimal value) { paidAmount = value; } public BillStatus getStatus() { return status; } public void setStatus(BillStatus value) { status = value; } public LocalDateTime getCreatedAt() { return createdAt; }
}
