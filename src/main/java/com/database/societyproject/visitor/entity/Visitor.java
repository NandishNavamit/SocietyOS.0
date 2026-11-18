package com.database.societyproject.visitor.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "visitors") public class Visitor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String fullName;
    private String phoneNumber; private String vehicleNumber;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; } public String getFullName() { return fullName; } public void setFullName(String v) { fullName = v; } public String getPhoneNumber() { return phoneNumber; } public void setPhoneNumber(String v) { phoneNumber = v; } public String getVehicleNumber() { return vehicleNumber; } public void setVehicleNumber(String v) { vehicleNumber = v; } public LocalDateTime getCreatedAt() { return createdAt; }
}
