package com.database.societyproject.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "society_registration_enquiries")
public class SocietyRegistrationEnquiry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String contactName;
    @Column(nullable = false) private String email;
    @Column(nullable = false) private String phoneNumber;
    @Column(nullable = false) private String societyName;
    private String city;
    private Integer estimatedBlocks;
    private Integer estimatedFlats;
    @Column(length = 2000) private String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EnquiryStatus status = EnquiryStatus.NEW;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @PrePersist void prepare() { if (createdAt == null) createdAt = LocalDateTime.now(); if (status == null) status = EnquiryStatus.NEW; }
    public Long getId() { return id; } public String getContactName() { return contactName; } public void setContactName(String value) { contactName = value; }
    public String getEmail() { return email; } public void setEmail(String value) { email = value; } public String getPhoneNumber() { return phoneNumber; } public void setPhoneNumber(String value) { phoneNumber = value; }
    public String getSocietyName() { return societyName; } public void setSocietyName(String value) { societyName = value; } public String getCity() { return city; } public void setCity(String value) { city = value; }
    public Integer getEstimatedBlocks() { return estimatedBlocks; } public void setEstimatedBlocks(Integer value) { estimatedBlocks = value; } public Integer getEstimatedFlats() { return estimatedFlats; } public void setEstimatedFlats(Integer value) { estimatedFlats = value; }
    public String getMessage() { return message; } public void setMessage(String value) { message = value; } public EnquiryStatus getStatus() { return status; } public void setStatus(EnquiryStatus value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
