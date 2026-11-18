package com.database.societyproject.complaint.entity;

import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.staff.entity.StaffAssignment;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "society_id", nullable = false) private
    Society society;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raised_by_resident_id", nullable = false)
    private Resident raisedByResident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_assignment_id")
    private StaffAssignment assignedStaffAssignment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintPriority priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; if (status == null) status = ComplaintStatus.OPEN; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public Society getSociety() { return society; } public void setSociety(Society value) { society = value; } public Flat getFlat() { return flat; } public void setFlat(Flat value) { flat = value; }
    public Resident getRaisedByResident() { return raisedByResident; } public void setRaisedByResident(Resident value) { raisedByResident = value; } public StaffAssignment getAssignedStaffAssignment() { return assignedStaffAssignment; } public void setAssignedStaffAssignment(StaffAssignment value) { assignedStaffAssignment = value; }
    public ComplaintCategory getCategory() { return category; } public void setCategory(ComplaintCategory value) { category = value; } public String getTitle() { return title; } public void setTitle(String value) { title = value; } public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    public ComplaintPriority getPriority() { return priority; } public void setPriority(ComplaintPriority value) { priority = value; } public ComplaintStatus getStatus() { return status; } public void setStatus(ComplaintStatus value) { status = value; } public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; } public LocalDateTime getResolvedAt() { return resolvedAt; } public void setResolvedAt(LocalDateTime value) { resolvedAt = value; } public LocalDateTime getClosedAt() { return closedAt; } public void setClosedAt(LocalDateTime value) { closedAt = value; }
}
