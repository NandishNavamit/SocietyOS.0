package com.database.societyproject.block.entity;

import com.database.societyproject.society.entity.Society;

import jakarta.persistence.*;

@Entity
@Table(name = "blocks", uniqueConstraints = @UniqueConstraint(name = "uk_block_society_name", columnNames = {"society_id", "name"}))
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(nullable =false)
    private String name;

    private Integer totalFloors;

    private Integer totalFlats;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = java.time.LocalDateTime.now();
        if (active == null) active = true;
    }

    public Long getId() { return id; }
    public Society getSociety() { return society; }
    public void setSociety(Society society) { this.society = society; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
    public Integer getTotalFlats() { return totalFlats; }
    public void setTotalFlats(Integer totalFlats) { this.totalFlats = totalFlats; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
}
