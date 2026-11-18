package com.database.societyproject.resident.dto;

import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import java.time.LocalDate;

public record ResidentFlatSummary(Long flatId, Long societyId, String societyName, String blockName,
                                  String flatNumber, OccupancyType occupancyType, ResidentRole residentRole,
                                  LocalDate startDate) {}
