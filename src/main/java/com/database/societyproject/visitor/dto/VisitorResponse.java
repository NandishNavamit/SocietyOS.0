package com.database.societyproject.visitor.dto;
import java.time.LocalDateTime;
public record VisitorResponse(Long id, String fullName, String phoneNumber, String vehicleNumber, LocalDateTime createdAt) { }
