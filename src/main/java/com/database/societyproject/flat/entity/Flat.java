package com.database.societyproject.flat.entity;

import com.database.societyproject.block.entity.Block;

import jakarta.persistence.*;

@Entity
@Table(name = "flats", uniqueConstraints = @UniqueConstraint(name = "uk_flat_block_number", columnNames = {"block_id", "flat_number"}))
public class Flat {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "block_id", nullable = false)
    private Block block;

    @Column(nullable=false)
    private String flatNumber;

    private Integer floorNumber;

    private String flatType;

    private Double areaSqft;

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
    public Block getBlock() { return block; }
    public void setBlock(Block block) { this.block = block; }
    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }
    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
    public String getFlatType() { return flatType; }
    public void setFlatType(String flatType) { this.flatType = flatType; }
    public Double getAreaSqft() { return areaSqft; }
    public void setAreaSqft(Double areaSqft) { this.areaSqft = areaSqft; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
}
