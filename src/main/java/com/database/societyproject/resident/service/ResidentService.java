package com.database.societyproject.resident.service;

import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.resident.dto.ResidentRequest;
import com.database.societyproject.resident.dto.ResidentResponse;
import com.database.societyproject.resident.repository.ResidentRepository;
import com.database.societyproject.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResidentService {
    private final ResidentRepository repository;
    private final UserService userService;
    public ResidentService(ResidentRepository repository, UserService userService) { this.repository = repository; this.userService = userService; }
    public ResidentResponse create(ResidentRequest request) {
        ensureUserUnused(request.userId(), null); Resident resident = new Resident(); apply(request, resident); return toResponse(repository.save(resident));
    }
    @Transactional(readOnly = true) public List<ResidentResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public ResidentResponse findById(Long id) { return toResponse(findEntity(id)); }
    @Transactional(readOnly = true) public ResidentResponse findByUserId(Long userId) { return toResponse(findEntityByUserId(userId)); }
    public ResidentResponse ensureProfile(Long userId) {
        return repository.findByUserId(userId).map(this::toResponse).orElseGet(() -> {
            Resident resident = new Resident();
            resident.setUser(userService.findEntity(userId));
            resident.setActive(true);
            return toResponse(repository.save(resident));
        });
    }
    public ResidentResponse update(Long id, ResidentRequest request) { Resident resident = findEntity(id); ensureUserUnused(request.userId(), id); apply(request, resident); return toResponse(resident); }
    public void deactivate(Long id) { findEntity(id).setActive(false); }
    public Resident findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resident", id)); }
    public Resident findEntityByUserId(Long userId) {
        return repository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Resident profile for user", userId));
    }
    private void ensureUserUnused(Long userId, Long residentId) {
        boolean exists = repository.existsByUserId(userId);
        if (exists && (residentId == null || !findEntity(residentId).getUser().getId().equals(userId))) {
            throw new DuplicateResourceException("This user already has a resident profile");
        }
    }
    private void apply(ResidentRequest request, Resident resident) {
        User user = userService.findEntity(request.userId()); resident.setUser(user);
        if (request.active() != null) resident.setActive(request.active());
    }
    private ResidentResponse toResponse(Resident entity) { return new ResidentResponse(entity.getId(), entity.getUser().getId(), entity.getUser().getFullName(), entity.getActive(), entity.getCreatedAt()); }
}
