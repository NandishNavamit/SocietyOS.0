package com.database.societyproject.logbook.dto;

import java.time.LocalDateTime;

public record LogbookEntryResponse(Long id, Long societyId, Long flatId, String flatNumber,
        String visitorName, String phoneNumber, String vehicleNumber, String purpose,
        String enteredByName, String exitedByName, LocalDateTime enteredAt, LocalDateTime exitedAt) { }
