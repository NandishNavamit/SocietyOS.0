package com.database.societyproject.complaint.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "complaint_status_history")
public class ComplaintStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "complaint_id", nullable = false) private Complaint complaint;
    @Enumerated(EnumType.STRING) private ComplaintStatus previousStatus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ComplaintStatus newStatus;
    @Column(length = 1000) private String note;
    @Column(nullable = false, updatable = false) private LocalDateTime changedAt;
    @PrePersist void prePersist() { if (changedAt == null) changedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public Complaint getComplaint() { return complaint; } public void setComplaint(Complaint value) { complaint = value; } public ComplaintStatus getPreviousStatus() { return previousStatus; } public void setPreviousStatus(ComplaintStatus value) { previousStatus = value; } public ComplaintStatus getNewStatus() { return newStatus; } public void setNewStatus(ComplaintStatus value) { newStatus = value; } public String getNote() { return note; } public void setNote(String value) { note = value; } public LocalDateTime getChangedAt() { return changedAt; }
}
