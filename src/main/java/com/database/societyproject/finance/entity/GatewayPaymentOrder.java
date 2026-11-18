package com.database.societyproject.finance.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="gateway_payment_orders", uniqueConstraints=@UniqueConstraint(columnNames="razorpay_order_id")) public class GatewayPaymentOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="bill_id",nullable=false) private MaintenanceBill bill;
 @Column(name="razorpay_order_id",nullable=false) private String razorpayOrderId; @Column(nullable=false) private Long amountPaise;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private GatewayOrderStatus status=GatewayOrderStatus.CREATED; private String razorpayPaymentId; @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
 @PrePersist void p(){if(createdAt==null)createdAt=LocalDateTime.now();} public Long getId(){return id;} public MaintenanceBill getBill(){return bill;} public void setBill(MaintenanceBill v){bill=v;} public String getRazorpayOrderId(){return razorpayOrderId;} public void setRazorpayOrderId(String v){razorpayOrderId=v;} public Long getAmountPaise(){return amountPaise;} public void setAmountPaise(Long v){amountPaise=v;} public GatewayOrderStatus getStatus(){return status;} public void setStatus(GatewayOrderStatus v){status=v;} public String getRazorpayPaymentId(){return razorpayPaymentId;} public void setRazorpayPaymentId(String v){razorpayPaymentId=v;}
}
