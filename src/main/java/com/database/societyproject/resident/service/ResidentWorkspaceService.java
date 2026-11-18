package com.database.societyproject.resident.service;

import com.database.societyproject.finance.dto.MaintenanceBillResponse;
import com.database.societyproject.finance.service.MaintenanceBillService;
import com.database.societyproject.notification.dto.NotificationResponse;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.occupancy.entity.FlatOccupancy;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.dto.*;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResidentWorkspaceService {
    private final ResidentService residents;
    private final FlatOccupancyRepository occupancies;
    private final MaintenanceBillService bills;
    private final NotificationService notifications;
    private final SocietyAuthorizationService authorization;

    public ResidentWorkspaceService(ResidentService residents, FlatOccupancyRepository occupancies,
                                    MaintenanceBillService bills, NotificationService notifications,
                                    SocietyAuthorizationService authorization) {
        this.residents = residents; this.occupancies = occupancies; this.bills = bills;
        this.notifications = notifications; this.authorization = authorization;
    }

    @Transactional
    public ResidentWorkspaceResponse mine() {
        Long userId = authorization.currentUserId();
        residents.ensureProfile(userId);
        Resident resident = residents.findEntityByUserId(userId);
        List<FlatOccupancy> active = occupancies.findByResidentId(resident.getId()).stream()
                .filter(occupancy -> Boolean.TRUE.equals(occupancy.getActive())).toList();
        List<ResidentFlatSummary> flats = active.stream().map(this::flat).toList();
        List<MaintenanceBillResponse> maintenanceBills = active.stream()
                .map(occupancy -> occupancy.getFlat().getId()).distinct()
                .flatMap(flatId -> bills.findByFlatIdForResidentWorkspace(flatId).stream()).toList();
        List<NotificationResponse> inbox = notifications.findByUserId(userId, false);
        return new ResidentWorkspaceResponse(residents.findByUserId(userId), flats, maintenanceBills, inbox);
    }

    private ResidentFlatSummary flat(FlatOccupancy occupancy) {
        var flat = occupancy.getFlat();
        var block = flat.getBlock();
        return new ResidentFlatSummary(flat.getId(), block.getSociety().getId(), block.getSociety().getName(),
                block.getName(), flat.getFlatNumber(), occupancy.getOccupancyType(), occupancy.getResidentRole(),
                occupancy.getStartDate());
    }
}
