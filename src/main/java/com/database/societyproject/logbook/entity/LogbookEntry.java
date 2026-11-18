package com.database.societyproject.logbook.entity;

import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A deliberately private gate register. It is unrelated to the retired visitor/security model. */
@Entity
@Table(name = "logbook_entries", indexes = @Index(name = "idx_logbook_society_entered", columnList = "society_id,entered_at"))
public class LogbookEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "society_id", nullable = false) private Society society;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "flat_id") private Flat destinationFlat;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "entered_by_user_id", nullable = false) private User enteredByUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "exited_by_user_id") private User exitedByUser;
    @Column(nullable = false, length = 160) private String visitorName;
    @Column(length = 40) private String phoneNumber;
    @Column(length = 80) private String vehicleNumber;
    @Column(nullable = false, length = 500) private String purpose;
    @Column(nullable = false, updatable = false) private LocalDateTime enteredAt;
    private LocalDateTime exitedAt;
    @PrePersist void prePersist() { if (enteredAt == null) enteredAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Society getSociety() { return society; } public void setSociety(Society value) { society = value; }
    public Flat getDestinationFlat() { return destinationFlat; } public void setDestinationFlat(Flat value) { destinationFlat = value; }
    public User getEnteredByUser() { return enteredByUser; } public void setEnteredByUser(User value) { enteredByUser = value; }
    public User getExitedByUser() { return exitedByUser; } public void setExitedByUser(User value) { exitedByUser = value; }
    public String getVisitorName() { return visitorName; } public void setVisitorName(String value) { visitorName = value; }
    public String getPhoneNumber() { return phoneNumber; } public void setPhoneNumber(String value) { phoneNumber = value; }
    public String getVehicleNumber() { return vehicleNumber; } public void setVehicleNumber(String value) { vehicleNumber = value; }
    public String getPurpose() { return purpose; } public void setPurpose(String value) { purpose = value; }
    public LocalDateTime getEnteredAt() { return enteredAt; } public LocalDateTime getExitedAt() { return exitedAt; } public void setExitedAt(LocalDateTime value) { exitedAt = value; }
}
