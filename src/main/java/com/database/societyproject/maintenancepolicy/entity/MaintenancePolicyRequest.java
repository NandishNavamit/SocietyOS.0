package com.database.societyproject.maintenancepolicy.entity;

import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_policy_requests", indexes = {
        @Index(name = "idx_policy_society_effective", columnList = "society_id,effective_month"),
        @Index(name = "idx_policy_status", columnList = "status")
})
public class MaintenancePolicyRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal monthlyAmount;
    @Column(nullable = false) private Integer dueDayOfMonth;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal lateFeePerOverdueMonth;
    @Column(nullable = false) private LocalDate effectiveMonth;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MaintenancePolicyStatus status = MaintenancePolicyStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "submitted_by_user_id", nullable = false) private User submittedBy;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by_user_id") private User reviewedBy;
    @Column(length = 1000) private String reviewNote;
    @Column(nullable = false, updatable = false) private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    @PrePersist void prepare() { if (submittedAt == null) submittedAt = LocalDateTime.now(); if (status == null) status = MaintenancePolicyStatus.PENDING; }
    public Long getId() { return id; } public Society getSociety() { return society; } public void setSociety(Society value) { society = value; }
    public BigDecimal getMonthlyAmount() { return monthlyAmount; } public void setMonthlyAmount(BigDecimal value) { monthlyAmount = value; }
    public Integer getDueDayOfMonth() { return dueDayOfMonth; } public void setDueDayOfMonth(Integer value) { dueDayOfMonth = value; }
    public BigDecimal getLateFeePerOverdueMonth() { return lateFeePerOverdueMonth; } public void setLateFeePerOverdueMonth(BigDecimal value) { lateFeePerOverdueMonth = value; }
    public LocalDate getEffectiveMonth() { return effectiveMonth; } public void setEffectiveMonth(LocalDate value) { effectiveMonth = value; }
    public MaintenancePolicyStatus getStatus() { return status; } public void setStatus(MaintenancePolicyStatus value) { status = value; }
    public User getSubmittedBy() { return submittedBy; } public void setSubmittedBy(User value) { submittedBy = value; }
    public User getReviewedBy() { return reviewedBy; } public void setReviewedBy(User value) { reviewedBy = value; }
    public String getReviewNote() { return reviewNote; } public void setReviewNote(String value) { reviewNote = value; }
    public LocalDateTime getSubmittedAt() { return submittedAt; } public LocalDateTime getReviewedAt() { return reviewedAt; } public void setReviewedAt(LocalDateTime value) { reviewedAt = value; }
}
