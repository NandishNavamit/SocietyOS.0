package com.database.societyproject.generalcomplaint.entity;

import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "general_complaints", indexes = {
        @Index(name = "idx_general_complaint_society_created", columnList = "society_id,created_at"),
        @Index(name = "idx_general_complaint_owner_created", columnList = "raised_by_user_id,created_at")
})
public class GeneralComplaint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "flat_id", nullable = false) private Flat flat;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "raised_by_user_id", nullable = false) private User raisedByUser;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 3000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private GeneralComplaintStatus status = GeneralComplaintStatus.OPEN;
    @Column(length = 3000) private String resolutionNote;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; if (status == null) status = GeneralComplaintStatus.OPEN; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Society getSociety() { return society; } public void setSociety(Society value) { society = value; }
    public Flat getFlat() { return flat; } public void setFlat(Flat value) { flat = value; }
    public User getRaisedByUser() { return raisedByUser; } public void setRaisedByUser(User value) { raisedByUser = value; }
    public String getTitle() { return title; } public void setTitle(String value) { title = value; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    public GeneralComplaintStatus getStatus() { return status; } public void setStatus(GeneralComplaintStatus value) { status = value; }
    public String getResolutionNote() { return resolutionNote; } public void setResolutionNote(String value) { resolutionNote = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; } public void setResolvedAt(LocalDateTime value) { resolvedAt = value; }
    public LocalDateTime getClosedAt() { return closedAt; } public void setClosedAt(LocalDateTime value) { closedAt = value; }
}
