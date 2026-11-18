package com.database.societyproject.staff.dto;
import com.database.societyproject.staff.entity.StaffType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public record StaffAssignmentRequest(@NotNull Long staffProfileId, @NotNull Long societyId,
                                     @NotNull StaffType staffType, LocalDate joiningDate, Boolean active) { }
