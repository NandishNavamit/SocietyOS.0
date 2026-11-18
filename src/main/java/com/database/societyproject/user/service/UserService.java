package com.database.societyproject.user.service;

import com.database.societyproject.user.entity.User;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.user.dto.UserRequest;
import com.database.societyproject.user.dto.UserResponse;
import com.database.societyproject.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository repository;
    public UserService(UserRepository repository) { this.repository = repository; }
    public UserResponse create(UserRequest request) { User user = new User(); apply(request, user); return toResponse(repository.save(user)); }
    @Transactional(readOnly = true) public List<UserResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public UserResponse findById(Long id) { return toResponse(findEntity(id)); }
    public UserResponse update(Long id, UserRequest request) { User user = findEntity(id); apply(request, user); return toResponse(user); }
    public void deactivate(Long id) { findEntity(id).setActive(false); }
    public User findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id)); }
    private void apply(UserRequest request, User user) {
        user.setFullName(request.fullName()); user.setEmail(request.email()); user.setPhoneNumber(request.phoneNumber());
        if (request.active() != null) user.setActive(request.active());
    }
    private UserResponse toResponse(User entity) { return new UserResponse(entity.getId(), entity.getFullName(), entity.getEmail(), entity.getPhoneNumber(), entity.getActive(), entity.getCreatedAt()); }
}
