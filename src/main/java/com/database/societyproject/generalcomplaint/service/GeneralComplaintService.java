package com.database.societyproject.generalcomplaint.service;

import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.generalcomplaint.dto.*;
import com.database.societyproject.generalcomplaint.entity.*;
import com.database.societyproject.generalcomplaint.repository.GeneralComplaintRepository;
import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GeneralComplaintService {
    private final GeneralComplaintRepository complaints; private final FlatRepository flats; private final UserRepository users;
    private final FlatOccupancyRepository occupancies; private final SocietyAuthorizationService authorization;
    private final NotificationService notifications;
    public GeneralComplaintService(GeneralComplaintRepository complaints, FlatRepository flats, UserRepository users,
            FlatOccupancyRepository occupancies, SocietyAuthorizationService authorization, NotificationService notifications) {
        this.complaints = complaints; this.flats = flats; this.users = users; this.occupancies = occupancies; this.authorization = authorization; this.notifications = notifications;
    }
    public GeneralComplaintResponse create(GeneralComplaintCreateRequest request) {
        Long userId = authorization.currentUserId();
        Flat flat = flats.findById(request.flatId()).orElseThrow(() -> new ResourceNotFoundException("Flat", request.flatId()));
        if (!Boolean.TRUE.equals(flat.getActive()) || !occupancies.findByResidentUserId(userId).stream().anyMatch(item ->
                Boolean.TRUE.equals(item.getActive()) && item.getFlat().getId().equals(flat.getId()) &&
                item.getOccupancyType() == OccupancyType.OWNER && item.getResidentRole() == ResidentRole.PRIMARY)) {
            throw new InvalidRequestException("You can raise a complaint only for one of your active flats");
        }
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        GeneralComplaint complaint = new GeneralComplaint(); complaint.setSociety(flat.getBlock().getSociety()); complaint.setFlat(flat);
        complaint.setRaisedByUser(user); complaint.setTitle(request.title().trim()); complaint.setDescription(request.description().trim());
        GeneralComplaint saved = complaints.save(complaint); notifications.notifyGeneralComplaintCreated(saved); return response(saved);
    }
    @Transactional(readOnly = true) public List<GeneralComplaintResponse> mine() {
        return complaints.findByRaisedByUserIdOrderByCreatedAtDesc(authorization.currentUserId()).stream().map(this::response).toList();
    }
    @Transactional(readOnly = true) public List<GeneralComplaintResponse> bySociety(Long societyId) {
        authorization.requireManager(societyId);
        return complaints.findBySocietyIdOrderByCreatedAtDesc(societyId).stream().map(this::response).toList();
    }
    public GeneralComplaintResponse update(Long id, GeneralComplaintUpdateRequest request) {
        GeneralComplaint complaint = complaints.findById(id).orElseThrow(() -> new ResourceNotFoundException("General complaint", id));
        authorization.requireManager(complaint.getSociety().getId());
        if (!isNext(complaint.getStatus(), request.status())) throw new InvalidRequestException("Use the next complaint status in order: open, in progress, resolved, then closed.");
        String note = trim(request.resolutionNote());
        if (request.status() == GeneralComplaintStatus.RESOLVED && note == null) throw new InvalidRequestException("A resolution note is required when resolving a complaint");
        complaint.setStatus(request.status());
        if (note != null) complaint.setResolutionNote(note);
        if (request.status() == GeneralComplaintStatus.RESOLVED) complaint.setResolvedAt(LocalDateTime.now());
        if (request.status() == GeneralComplaintStatus.CLOSED) complaint.setClosedAt(LocalDateTime.now());
        notifications.notifyGeneralComplaintUpdated(complaint); return response(complaint);
    }
    private boolean isNext(GeneralComplaintStatus current, GeneralComplaintStatus target) {
        return (current == GeneralComplaintStatus.OPEN && target == GeneralComplaintStatus.IN_PROGRESS)
                || (current == GeneralComplaintStatus.IN_PROGRESS && target == GeneralComplaintStatus.RESOLVED)
                || (current == GeneralComplaintStatus.RESOLVED && target == GeneralComplaintStatus.CLOSED);
    }
    private String trim(String value) { if (value == null) return null; String trimmed = value.trim(); return trimmed.isEmpty() ? null : trimmed; }
    private GeneralComplaintResponse response(GeneralComplaint item) { return new GeneralComplaintResponse(item.getId(), item.getSociety().getId(), item.getSociety().getName(), item.getFlat().getId(), item.getFlat().getFlatNumber(), item.getRaisedByUser().getFullName(), item.getTitle(), item.getDescription(), item.getStatus(), item.getResolutionNote(), item.getCreatedAt(), item.getUpdatedAt(), item.getResolvedAt(), item.getClosedAt()); }
}
