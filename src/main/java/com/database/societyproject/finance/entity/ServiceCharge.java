package com.database.societyproject.finance.entity;
import com.database.societyproject.complaint.entity.Complaint;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="service_charges", indexes={@Index(name="idx_service_charge_society",columnList="society_id,status"),@Index(name="idx_service_charge_complaint",columnList="complaint_id")}) public class ServiceCharge {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="society_id",nullable=false) private Society society;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="complaint_id",nullable=false) private Complaint complaint;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="bill_id",nullable=false,unique=true) private MaintenanceBill bill;
 @Column(nullable=false,length=1000) private String description; @Column(nullable=false,precision=12,scale=2) private BigDecimal amount; private String vendorName; private String invoiceNumber; @Column(length=2000) private String internalNote;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private ServiceChargeStatus status=ServiceChargeStatus.ISSUED;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="created_by_user_id",nullable=false) private User createdBy; @Column(nullable=false,updatable=false) private LocalDateTime createdAt; private LocalDateTime voidedAt;
 @PrePersist void p(){if(createdAt==null)createdAt=LocalDateTime.now();if(status==null)status=ServiceChargeStatus.ISSUED;}
 public Long getId(){return id;} public Society getSociety(){return society;} public void setSociety(Society v){society=v;} public Complaint getComplaint(){return complaint;} public void setComplaint(Complaint v){complaint=v;} public MaintenanceBill getBill(){return bill;} public void setBill(MaintenanceBill v){bill=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public String getVendorName(){return vendorName;} public void setVendorName(String v){vendorName=v;} public String getInvoiceNumber(){return invoiceNumber;} public void setInvoiceNumber(String v){invoiceNumber=v;} public String getInternalNote(){return internalNote;} public void setInternalNote(String v){internalNote=v;} public ServiceChargeStatus getStatus(){return status;} public void setStatus(ServiceChargeStatus v){status=v;} public User getCreatedBy(){return createdBy;} public void setCreatedBy(User v){createdBy=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getVoidedAt(){return voidedAt;} public void setVoidedAt(LocalDateTime v){voidedAt=v;}
}
