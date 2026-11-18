package com.database.societyproject.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bill_id", nullable = false) private MaintenanceBill bill;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private LocalDate paymentDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentStatus status = PaymentStatus.COMPLETED;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (status == null) status = PaymentStatus.COMPLETED; }
    public Long getId() { return id; } public MaintenanceBill getBill() { return bill; } public void setBill(MaintenanceBill bill) { this.bill = bill; } public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getPaymentDate() { return paymentDate; } public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; } public PaymentMethod getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(PaymentMethod value) { paymentMethod = value; }
    public String getReferenceNumber() { return referenceNumber; } public void setReferenceNumber(String value) { referenceNumber = value; } public String getNotes() { return notes; } public void setNotes(String value) { notes = value; } public PaymentStatus getStatus() { return status; } public void setStatus(PaymentStatus value) { status = value; } public LocalDateTime getCreatedAt() { return createdAt; }
}
