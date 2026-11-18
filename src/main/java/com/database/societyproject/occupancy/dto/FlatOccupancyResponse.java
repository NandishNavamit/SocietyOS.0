package com.database.societyproject.occupancy.dto;

import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FlatOccupancyResponse(Long id, Long flatId, Long residentId, OccupancyType occupancyType,
                                    ResidentRole residentRole, LocalDate startDate, LocalDate endDate,
                                    Boolean active, LocalDateTime createdAt) { }
