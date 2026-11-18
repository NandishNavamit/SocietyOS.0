package com.database.societyproject.society.dto;

import java.time.LocalDateTime;
public record SocietyResponse(Long id, String name, String address, String city, String state,
                              String pincode, Boolean active, LocalDateTime createdAt) { }
