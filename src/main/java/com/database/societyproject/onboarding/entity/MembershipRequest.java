package com.database.societyproject.onboarding.entity;

import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership_requests", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "society_id", "requested_role", "status"}))
public class MembershipRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @Enumerated(EnumType.STRING) @Column(name = "requested_role", nullable = false) private UserRole requestedRole;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private MembershipRequestStatus status = MembershipRequestStatus.PENDING;
    @Column(length = 1000) private String note;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prepare() { if (createdAt == null) createdAt = LocalDateTime.now(); if (status == null) status = MembershipRequestStatus.PENDING; }
    public Long getId() { return id; } public User getUser() { return user; } public void setUser(User value) { user = value; }
    public Society getSociety() { return society; } public void setSociety(Society value) { society = value; } public UserRole getRequestedRole() { return requestedRole; } public void setRequestedRole(UserRole value) { requestedRole = value; }
    public MembershipRequestStatus getStatus() { return status; } public void setStatus(MembershipRequestStatus value) { status = value; } public String getNote() { return note; } public void setNote(String value) { note = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
