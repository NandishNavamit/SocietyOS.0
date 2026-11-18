package com.database.societyproject.security.controller;

import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/register")
    public ResponseEntity<CurrentUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
    @PostMapping("/refresh") public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) { return service.refresh(request); }
    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(@Valid @RequestBody RefreshRequest request) { service.logout(request); }
    @PostMapping("/change-password") @ResponseStatus(HttpStatus.NO_CONTENT) public void changePassword(@Valid @RequestBody ChangePasswordRequest request) { service.changePassword(request); }
    @GetMapping("/me") public CurrentUserResponse me() { return service.currentUser(); }
}
