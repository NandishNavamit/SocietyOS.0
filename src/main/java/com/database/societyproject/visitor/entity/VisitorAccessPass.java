package com.database.societyproject.visitor.entity;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.society.entity.Society;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name = "visitor_access_passes") public class VisitorAccessPass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "visitor_id", nullable = false) private Visitor visitor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "flat_id") private Flat flat;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_resident_id") private Resident hostResident;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AccessType accessType;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AccessPassStatus status = AccessPassStatus.PENDING;
    @Column(nullable = false) private LocalDate validFrom; private LocalDate validUntil;
    @Column(nullable = false) private String purpose;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (status == null) status = AccessPassStatus.PENDING; }
    public Long getId() { return id; } public Visitor getVisitor() { return visitor; } public void setVisitor(Visitor v) { visitor = v; } public Society getSociety() { return society; } public void setSociety(Society v) { society = v; } public Flat getFlat() { return flat; } public void setFlat(Flat v) { flat = v; } public Resident getHostResident() { return hostResident; } public void setHostResident(Resident v) { hostResident = v; } public AccessType getAccessType() { return accessType; } public void setAccessType(AccessType v) { accessType = v; } public AccessPassStatus getStatus() { return status; } public void setStatus(AccessPassStatus v) { status = v; } public LocalDate getValidFrom() { return validFrom; } public void setValidFrom(LocalDate v) { validFrom = v; } public LocalDate getValidUntil() { return validUntil; } public void setValidUntil(LocalDate v) { validUntil = v; } public String getPurpose() { return purpose; } public void setPurpose(String v) { purpose = v; } public LocalDateTime getCreatedAt() { return createdAt; }
}
