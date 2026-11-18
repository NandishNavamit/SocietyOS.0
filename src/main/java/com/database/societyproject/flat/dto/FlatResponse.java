package com.database.societyproject.flat.dto;

import java.time.LocalDateTime;
public record FlatResponse(Long id, String flatNumber, Integer floorNumber, String flatType,
                           Double areaSqft, Boolean active, LocalDateTime createdAt, Long blockId) { }
