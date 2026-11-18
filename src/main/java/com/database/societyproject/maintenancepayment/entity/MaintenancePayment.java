package com.database.societyproject.maintenancepayment.entity;

import com.database.societyproject.finance.entity.PaymentMethod;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name="monthly_maintenance_payments", indexes={@Index(name="idx_monthly_payment_society",columnList="society_id,created_at"),@Index(name="idx_monthly_payment_payer",columnList="payer_user_id")})
public class MaintenancePayment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="society_id",nullable=false) private Society society;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="payer_user_id") private User payer;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="recorded_by_user_id") private User recordedBy;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(nullable=false) private LocalDate paymentDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private PaymentMethod paymentMethod;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private MaintenancePaymentStatus status=MaintenancePaymentStatus.CREATED;
 @Column(unique=true) private String razorpayOrderId; @Column(unique=true) private String razorpayPaymentId;
 @Column(length=255) private String referenceNumber; @Column(length=1000) private String notes;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt; private LocalDateTime completedAt;
 @PrePersist void prepare(){if(createdAt==null)createdAt=LocalDateTime.now(); if(status==null)status=MaintenancePaymentStatus.CREATED;}
 public Long getId(){return id;} public Society getSociety(){return society;} public void setSociety(Society v){society=v;} public User getPayer(){return payer;} public void setPayer(User v){payer=v;} public User getRecordedBy(){return recordedBy;} public void setRecordedBy(User v){recordedBy=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public LocalDate getPaymentDate(){return paymentDate;} public void setPaymentDate(LocalDate v){paymentDate=v;} public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod v){paymentMethod=v;} public MaintenancePaymentStatus getStatus(){return status;} public void setStatus(MaintenancePaymentStatus v){status=v;} public String getRazorpayOrderId(){return razorpayOrderId;} public void setRazorpayOrderId(String v){razorpayOrderId=v;} public String getRazorpayPaymentId(){return razorpayPaymentId;} public void setRazorpayPaymentId(String v){razorpayPaymentId=v;} public String getReferenceNumber(){return referenceNumber;} public void setReferenceNumber(String v){referenceNumber=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
}
