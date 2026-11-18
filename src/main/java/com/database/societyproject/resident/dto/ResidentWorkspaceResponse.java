package com.database.societyproject.resident.dto;

import com.database.societyproject.finance.dto.MaintenanceBillResponse;
import com.database.societyproject.notification.dto.NotificationResponse;
import java.util.List;

public record ResidentWorkspaceResponse(ResidentResponse resident, List<ResidentFlatSummary> flats,
                                        List<MaintenanceBillResponse> bills,
                                        List<NotificationResponse> notifications) {}
