package com.database.societyproject.staff.entity;

import com.database.societyproject.society.entity.Society;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_assignments", uniqueConstraints = @UniqueConstraint(
        name = "uk_staff_profile_society", columnNames = {"staff_profile_id", "society_id"}))
public class StaffAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_profile_id", nullable = false)
    private StaffProfile staffProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private StaffType staffType;

    private LocalDate joiningDate;

    @Column(nullable = false) private Boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); if (active == null) active = true; }
    public Long getId() { return id; }

    public StaffProfile getStaffProfile() { return staffProfile; }
    public void setStaffProfile(StaffProfile staffProfile) { this.staffProfile = staffProfile; }
    public Society getSociety() { return society; }
    public void setSociety(Society society) { this.society = society; }
    public StaffType getStaffType() { return staffType; }
    public void setStaffType(StaffType staffType) { this.staffType = staffType; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
