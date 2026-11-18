package com.database.societyproject.finance.entity;

import com.database.societyproject.society.entity.Society;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "charge_plans")
public class ChargePlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @Column(nullable = false) private String name;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ChargeFrequency frequency;
    private Integer dueDayOfMonth;
    @Column(nullable = false) private Boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (active == null) active = true; }
    public Long getId() { return id; } public Society getSociety() { return society; } public void setSociety(Society society) { this.society = society; }
    public String getName() { return name; } public void setName(String name) { this.name = name; } public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
    public ChargeFrequency getFrequency() { return frequency; } public void setFrequency(ChargeFrequency frequency) { this.frequency = frequency; } public Integer getDueDayOfMonth() { return dueDayOfMonth; } public void setDueDayOfMonth(Integer dueDayOfMonth) { this.dueDayOfMonth = dueDayOfMonth; }
    public Boolean getActive() { return active; } public void setActive(Boolean active) { this.active = active; } public LocalDateTime getCreatedAt() { return createdAt; }
}
