package com.database.societyproject.finance.entity;

import com.database.societyproject.society.entity.Society;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "society_expenses")
public class SocietyExpense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ExpenseCategory category;
    @Column(nullable = false) private String description;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private LocalDate expenseDate;
    private String vendorName;
    private String referenceNumber;
    @Column(nullable = false) private Boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (active == null) active = true; }
    public Long getId() { return id; } public Society getSociety() { return society; } public void setSociety(Society society) { this.society = society; } public ExpenseCategory getCategory() { return category; } public void setCategory(ExpenseCategory category) { this.category = category; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; } public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal value) { amount = value; } public LocalDate getExpenseDate() { return expenseDate; } public void setExpenseDate(LocalDate value) { expenseDate = value; }
    public String getVendorName() { return vendorName; } public void setVendorName(String value) { vendorName = value; } public String getReferenceNumber() { return referenceNumber; } public void setReferenceNumber(String value) { referenceNumber = value; } public Boolean getActive() { return active; } public void setActive(Boolean value) { active = value; } public LocalDateTime getCreatedAt() { return createdAt; }
}
