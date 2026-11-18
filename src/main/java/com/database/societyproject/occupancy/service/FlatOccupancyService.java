package com.database.societyproject.occupancy.service;

import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.occupancy.entity.FlatOccupancy;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.dto.FlatOccupancyRequest;
import com.database.societyproject.occupancy.dto.FlatOccupancyResponse;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.service.ResidentService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FlatOccupancyService {
    private final FlatOccupancyRepository repository;
    private final FlatService flatService;
    private final ResidentService residentService;
    public FlatOccupancyService(FlatOccupancyRepository repository, FlatService flatService, ResidentService residentService) {
        this.repository = repository; this.flatService = flatService; this.residentService = residentService;
    }
    public FlatOccupancyResponse create(FlatOccupancyRequest request) {
        validateDates(request); ensureNoActiveDuplicate(request.flatId(), request.residentId(), request.active(), null);
        FlatOccupancy occupancy = new FlatOccupancy(); apply(request, occupancy); return toResponse(repository.save(occupancy));
    }
    @Transactional(readOnly = true) public List<FlatOccupancyResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public FlatOccupancyResponse findById(Long id) { return toResponse(findEntity(id)); }
    @Transactional(readOnly = true) public List<FlatOccupancyResponse> findByFlatId(Long flatId) { flatService.findEntity(flatId); return repository.findByFlatId(flatId).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<FlatOccupancyResponse> findByResidentId(Long residentId) { residentService.findEntity(residentId); return repository.findByResidentId(residentId).stream().map(this::toResponse).toList(); }
    public FlatOccupancyResponse update(Long id, FlatOccupancyRequest request) {
        validateDates(request); FlatOccupancy occupancy = findEntity(id);
        Boolean effectiveActive = request.active() == null ? occupancy.getActive() : request.active();
        ensureNoActiveDuplicate(request.flatId(), request.residentId(), effectiveActive, id);
        apply(request, occupancy); return toResponse(occupancy);
    }
    public void deactivate(Long id) { findEntity(id).setActive(false); }
    private FlatOccupancy findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flat occupancy", id)); }
    private void ensureNoActiveDuplicate(Long flatId, Long residentId, Boolean active, Long id) {
        if (Boolean.FALSE.equals(active)) return;
        boolean exists = id == null ? repository.existsByFlatIdAndResidentIdAndActiveTrue(flatId, residentId)
                : repository.existsByFlatIdAndResidentIdAndActiveTrueAndIdNot(flatId, residentId, id);
        if (exists) throw new DuplicateResourceException("This resident already has an active occupancy record for this flat");
    }
    private void validateDates(FlatOccupancyRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) throw new InvalidRequestException("endDate cannot be before startDate");
    }
    private void apply(FlatOccupancyRequest request, FlatOccupancy occupancy) {
        Flat flat = flatService.findEntity(request.flatId()); Resident resident = residentService.findEntity(request.residentId());
        occupancy.setFlat(flat); occupancy.setResident(resident); occupancy.setOccupancyType(request.occupancyType()); occupancy.setResidentRole(request.residentRole());
        occupancy.setStartDate(request.startDate()); occupancy.setEndDate(request.endDate());
        if (request.active() != null) occupancy.setActive(request.active());
    }
    private FlatOccupancyResponse toResponse(FlatOccupancy entity) {
        return new FlatOccupancyResponse(entity.getId(), entity.getFlat().getId(), entity.getResident().getId(), entity.getOccupancyType(), entity.getResidentRole(), entity.getStartDate(), entity.getEndDate(), entity.getActive(), entity.getCreatedAt());
    }
}
