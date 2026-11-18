package com.database.societyproject.occupancy.dto;

import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record FlatOccupancyRequest(
        @NotNull Long flatId,
        @NotNull Long residentId,
        @NotNull OccupancyType occupancyType,
        @NotNull ResidentRole residentRole,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        Boolean active) { }
