package com.database.societyproject.security.entity;

import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_society_roles", uniqueConstraints = @UniqueConstraint(
        name = "uk_user_society_role", columnNames = {"user_id", "society_id", "role"}))
public class UserSocietyRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "society_id") private Society society;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UserRole role;
    @Column(nullable = false) private Boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;

    @PrePersist void prePersist() { if (active == null) active = true; if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Society getSociety() { return society; }
    public void setSociety(Society society) { this.society = society; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
