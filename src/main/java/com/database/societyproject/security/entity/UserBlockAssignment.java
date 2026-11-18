package com.database.societyproject.security.entity;

import com.database.societyproject.block.entity.Block;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_block_assignments", uniqueConstraints = @UniqueConstraint(
        name = "uk_user_block_assignment", columnNames = {"user_id", "block_id"}))
public class UserBlockAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "block_id", nullable = false) private Block block;
    @Column(nullable = false) private Boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prepare() { if (active == null) active = true; if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public User getUser() { return user; } public void setUser(User user) { this.user = user; }
    public Block getBlock() { return block; } public void setBlock(Block block) { this.block = block; }
    public Boolean getActive() { return active; } public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
