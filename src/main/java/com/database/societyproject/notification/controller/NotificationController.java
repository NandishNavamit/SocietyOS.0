package com.database.societyproject.notification.controller;

import com.database.societyproject.finance.service.MaintenanceBillService;
import com.database.societyproject.notification.dto.*;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    private final MaintenanceBillService billService;
    private final SocietyAuthorizationService authorization;

    public NotificationController(NotificationService service, MaintenanceBillService billService, SocietyAuthorizationService authorization) {
        this.service = service;
        this.billService = billService;
        this.authorization = authorization;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public ResponseEntity<List<NotificationResponse>> announcement(@Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAnnouncement(request));
    }

    @PostMapping("/maintenance-due/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public ResponseEntity<List<NotificationResponse>> maintenanceDue(@PathVariable Long billId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMaintenanceDue(billService.findEntity(billId)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> byUser(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "false") boolean unreadOnly) {
        if (!authorization.currentUserId().equals(userId)) throw new AccessDeniedException("You may only view your own notifications");
        return service.findByUserId(userId, unreadOnly);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> mine(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return service.findByUserId(authorization.currentUserId(), unreadOnly);
    }

    @GetMapping("/society/{societyId}")
    @PreAuthorize("hasAnyRole('ADMIN','SOCIETY_ADMIN')")
    public List<NotificationResponse> bySociety(@PathVariable Long societyId) {
        return service.findBySocietyId(societyId);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public NotificationResponse markRead(@PathVariable Long id) {
        if (!authorization.canReadNotification(id)) throw new AccessDeniedException("You may only update your own notifications");
        return service.markRead(id);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public java.util.Map<String, Long> markAllRead() {
        return java.util.Map.of("markedRead", service.markAllReadForCurrentUser());
    }
}
