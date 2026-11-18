package com.database.societyproject.onboarding.entity;

import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "society_invitations", indexes = {@Index(name = "idx_invitation_token", columnList = "token_hash", unique = true), @Index(name = "idx_invitation_society", columnList = "society_id,status")})
public class SocietyInvitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @Column(nullable = false) private String email;
    @Enumerated(EnumType.STRING) @Column(name = "invited_role", nullable = false) private UserRole invitedRole;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "flat_id") private Flat flat;
    @Column(name = "token_hash", nullable = false, length = 64) private String tokenHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SocietyInvitationStatus status = SocietyInvitationStatus.PENDING;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invited_by_user_id", nullable = false) private User invitedBy;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "accepted_by_user_id") private User acceptedBy;
    @Column(nullable = false) private LocalDateTime expiresAt;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    @PrePersist void prepare() { if (createdAt == null) createdAt = LocalDateTime.now(); if (status == null) status = SocietyInvitationStatus.PENDING; }
    public Long getId(){return id;} public Society getSociety(){return society;} public void setSociety(Society v){society=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public UserRole getInvitedRole(){return invitedRole;} public void setInvitedRole(UserRole v){invitedRole=v;} public Flat getFlat(){return flat;} public void setFlat(Flat v){flat=v;} public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;} public SocietyInvitationStatus getStatus(){return status;} public void setStatus(SocietyInvitationStatus v){status=v;} public User getInvitedBy(){return invitedBy;} public void setInvitedBy(User v){invitedBy=v;} public User getAcceptedBy(){return acceptedBy;} public void setAcceptedBy(User v){acceptedBy=v;} public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getAcceptedAt(){return acceptedAt;} public void setAcceptedAt(LocalDateTime v){acceptedAt=v;}
}
