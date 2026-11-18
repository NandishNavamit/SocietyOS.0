package com.database.societyproject.maintenancepolicy.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.maintenancepolicy.dto.*;
import com.database.societyproject.maintenancepolicy.entity.*;
import com.database.societyproject.maintenancepolicy.repository.MaintenancePolicyRequestRepository;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.user.repository.UserRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MaintenancePolicyService {
    private final MaintenancePolicyRequestRepository policies; private final SocietyService societies;
    private final SocietyAuthorizationService authorization; private final UserRepository users; private final NotificationService notifications;
    public MaintenancePolicyService(MaintenancePolicyRequestRepository policies, SocietyService societies, SocietyAuthorizationService authorization, UserRepository users, NotificationService notifications) { this.policies = policies; this.societies = societies; this.authorization = authorization; this.users = users; this.notifications = notifications; }
    public MaintenancePolicyResponse submit(MaintenancePolicyRequestCreate request) {
        authorization.requireManager(request.societyId());
        LocalDate effective = firstDay(request.effectiveMonth());
        if (effective.isBefore(YearMonth.now().atDay(1))) throw new InvalidRequestException("The effective month cannot be in the past");
        if (policies.existsBySocietyIdAndEffectiveMonthAndStatusIn(request.societyId(), effective, List.of(MaintenancePolicyStatus.PENDING, MaintenancePolicyStatus.APPROVED))) throw new DuplicateResourceException("A policy for this effective month is already awaiting review or approved");
        MaintenancePolicyRequest policy = new MaintenancePolicyRequest(); policy.setSociety(societies.findEntity(request.societyId()));
        policy.setMonthlyAmount(request.monthlyAmount()); policy.setDueDayOfMonth(request.dueDayOfMonth()); policy.setLateFeePerOverdueMonth(request.lateFeePerOverdueMonth()); policy.setEffectiveMonth(effective);
        policy.setSubmittedBy(users.getReferenceById(authorization.currentUserId())); return response(policies.save(policy));
    }
    @Transactional(readOnly = true) public List<MaintenancePolicyResponse> bySociety(Long societyId) { authorization.requireManager(societyId); return policies.findBySocietyIdOrderByEffectiveMonthDescSubmittedAtDesc(societyId).stream().map(this::response).toList(); }
    @Transactional(readOnly = true) public List<MaintenancePolicyResponse> pending() { authorization.requireAdmin(); return policies.findByStatusOrderBySubmittedAtAsc(MaintenancePolicyStatus.PENDING).stream().map(this::response).toList(); }
    public MaintenancePolicyResponse approve(Long id, MaintenancePolicyReviewRequest request) { return review(id, request, MaintenancePolicyStatus.APPROVED); }
    public MaintenancePolicyResponse reject(Long id, MaintenancePolicyReviewRequest request) { if (request.note() == null || request.note().isBlank()) throw new InvalidRequestException("Provide a reason when rejecting a policy"); return review(id, request, MaintenancePolicyStatus.REJECTED); }
    private MaintenancePolicyResponse review(Long id, MaintenancePolicyReviewRequest request, MaintenancePolicyStatus outcome) {
        authorization.requireAdmin(); MaintenancePolicyRequest policy = find(id);
        if (policy.getStatus() != MaintenancePolicyStatus.PENDING) throw new InvalidRequestException("Only a pending policy request can be reviewed");
        policy.setStatus(outcome); policy.setReviewedBy(users.getReferenceById(authorization.currentUserId())); policy.setReviewedAt(LocalDateTime.now()); policy.setReviewNote(blankToNull(request.note()));
        if (outcome == MaintenancePolicyStatus.APPROVED) notifications.notifyMaintenancePolicyApproved(policy);
        return response(policy);
    }
    private MaintenancePolicyRequest find(Long id) { return policies.findById(id).orElseThrow(() -> new ResourceNotFoundException("Maintenance policy request", id)); }
    private LocalDate firstDay(LocalDate date) { return date.withDayOfMonth(1); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private MaintenancePolicyResponse response(MaintenancePolicyRequest value) { return new MaintenancePolicyResponse(value.getId(), value.getSociety().getId(), value.getSociety().getName(), value.getMonthlyAmount(), value.getDueDayOfMonth(), value.getLateFeePerOverdueMonth(), value.getEffectiveMonth(), value.getStatus(), value.getSubmittedBy().getFullName(), value.getReviewedBy() == null ? null : value.getReviewedBy().getFullName(), value.getReviewNote(), value.getSubmittedAt(), value.getReviewedAt()); }
}
