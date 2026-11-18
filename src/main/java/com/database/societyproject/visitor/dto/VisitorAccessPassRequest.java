package com.database.societyproject.visitor.dto;
import com.database.societyproject.visitor.entity.AccessType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record VisitorAccessPassRequest(@NotNull Long visitorId, @NotNull Long societyId, Long flatId, Long hostResidentId, @NotNull AccessType accessType, @NotNull LocalDate validFrom, LocalDate validUntil, @NotBlank @Size(max = 1000) String purpose) { }
