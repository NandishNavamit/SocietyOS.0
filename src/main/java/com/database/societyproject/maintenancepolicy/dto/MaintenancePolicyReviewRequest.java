package com.database.societyproject.maintenancepolicy.dto;

import jakarta.validation.constraints.Size;

public record MaintenancePolicyReviewRequest(@Size(max = 1000) String note) { }
