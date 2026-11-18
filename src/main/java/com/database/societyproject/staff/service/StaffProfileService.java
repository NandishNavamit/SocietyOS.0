package com.database.societyproject.staff.service;

import com.database.societyproject.staff.entity.StaffProfile;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.staff.dto.StaffProfileRequest;
import com.database.societyproject.staff.dto.StaffProfileResponse;
import com.database.societyproject.staff.repository.StaffProfileRepository;
import com.database.societyproject.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class StaffProfileService {
    private final StaffProfileRepository repository; private final UserService userService;
    public StaffProfileService(StaffProfileRepository repository, UserService userService) { this.repository = repository; this.userService = userService; }
    public StaffProfileResponse create(StaffProfileRequest request) { StaffProfile profile = new StaffProfile(); apply(request, profile); return toResponse(repository.save(profile)); }
    @Transactional(readOnly = true) public List<StaffProfileResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public StaffProfileResponse findById(Long id) { return toResponse(findEntity(id)); }
    public StaffProfileResponse update(Long id, StaffProfileRequest request) { StaffProfile profile = findEntity(id); apply(request, profile); return toResponse(profile); }
    public StaffProfile findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff profile", id)); }
    private void apply(StaffProfileRequest request, StaffProfile profile) { profile.setFullName(request.fullName()); profile.setPhoneNumber(request.phoneNumber()); profile.setEmail(request.email()); profile.setUser(request.userId() == null ? null : userService.findEntity(request.userId())); }
    private StaffProfileResponse toResponse(StaffProfile entity) { return new StaffProfileResponse(entity.getId(), entity.getFullName(), entity.getPhoneNumber(), entity.getEmail(), entity.getUser() == null ? null : entity.getUser().getId(), entity.getCreatedAt()); }
}
