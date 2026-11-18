package com.database.societyproject.staff.dto;
import com.database.societyproject.staff.entity.StaffType;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record StaffAssignmentResponse(Long id, Long staffProfileId, Long userId, String staffName, Long societyId,
                                      StaffType staffType, LocalDate joiningDate, Boolean active, LocalDateTime createdAt) { }
