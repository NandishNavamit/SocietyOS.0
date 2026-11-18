package com.database.societyproject.visitor.entity;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.staff.entity.StaffAssignment;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name = "visitor_visits") public class VisitorVisit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "visitor_id", nullable = false) private Visitor visitor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "flat_id") private Flat flat;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_resident_id") private Resident hostResident;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "handled_by_security_id") private StaffAssignment handledBySecurity;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "access_pass_id") private VisitorAccessPass accessPass;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private VisitorType visitorType;
    @Column(nullable = false) private String purpose;
    private LocalDateTime entryTime; private LocalDateTime exitTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private VisitStatus status = VisitStatus.CHECKED_IN;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (status == null) status = VisitStatus.CHECKED_IN; }
    public Long getId() { return id; } public Visitor getVisitor() { return visitor; } public void setVisitor(Visitor v) { visitor = v; } public Society getSociety() { return society; } public void setSociety(Society v) { society = v; } public Flat getFlat() { return flat; } public void setFlat(Flat v) { flat = v; } public Resident getHostResident() { return hostResident; } public void setHostResident(Resident v) { hostResident = v; } public StaffAssignment getHandledBySecurity() { return handledBySecurity; } public void setHandledBySecurity(StaffAssignment v) { handledBySecurity = v; } public VisitorAccessPass getAccessPass() { return accessPass; } public void setAccessPass(VisitorAccessPass v) { accessPass = v; } public VisitorType getVisitorType() { return visitorType; } public void setVisitorType(VisitorType v) { visitorType = v; } public String getPurpose() { return purpose; } public void setPurpose(String v) { purpose = v; } public LocalDateTime getEntryTime() { return entryTime; } public void setEntryTime(LocalDateTime v) { entryTime = v; } public LocalDateTime getExitTime() { return exitTime; } public void setExitTime(LocalDateTime v) { exitTime = v; } public VisitStatus getStatus() { return status; } public void setStatus(VisitStatus v) { status = v; } public LocalDateTime getCreatedAt() { return createdAt; }
}
