package com.database.societyproject.visitor.dto;
import jakarta.validation.constraints.*;
public record VisitorRequest(@NotBlank @Size(max = 255) String fullName, @Pattern(regexp = "^$|^[0-9+() -]{7,20}$", message = "must be a valid phone number") String phoneNumber, @Size(max = 100) String vehicleNumber) { }
