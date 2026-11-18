package com.database.societyproject.logbook.service;

import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.logbook.dto.*;
import com.database.societyproject.logbook.entity.LogbookEntry;
import com.database.societyproject.logbook.repository.LogbookEntryRepository;
import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrivateLogbookService {
    private final LogbookEntryRepository entries; private final SocietyService societies; private final FlatRepository flats;
    private final UserRepository users; private final UserSocietyRoleRepository roles; private final SocietyAuthorizationService authorization;
    public PrivateLogbookService(LogbookEntryRepository entries, SocietyService societies, FlatRepository flats, UserRepository users,
            UserSocietyRoleRepository roles, SocietyAuthorizationService authorization) { this.entries = entries; this.societies = societies; this.flats = flats; this.users = users; this.roles = roles; this.authorization = authorization; }
    @Transactional(readOnly = true) public List<LogbookSocietyResponse> mySocieties() {
        Long userId = authorization.currentUserId();
        return roles.findByUserIdAndActiveTrue(userId).stream().filter(role -> role.getRole() == UserRole.LOGBOOK_OPERATOR && role.getSociety() != null)
                .map(role -> new LogbookSocietyResponse(role.getSociety().getId(), role.getSociety().getName())).toList();
    }
    @Transactional(readOnly = true) public List<LogbookEntryResponse> entries(Long societyId) { requireOperator(societyId); return entries.findBySocietyIdOrderByEnteredAtDesc(societyId).stream().map(this::response).toList(); }
    @Transactional(readOnly = true) public List<LogbookFlatResponse> flats(Long societyId) {
        requireOperator(societyId);
        return flats.findByBlockSocietyIdAndActiveTrue(societyId).stream().map(flat -> new LogbookFlatResponse(flat.getId(), flat.getFlatNumber(), flat.getBlock().getName())).toList();
    }
    public LogbookEntryResponse enter(LogbookEntryCreateRequest request) {
        Long userId = authorization.currentUserId(); requireOperator(request.societyId());
        Society society = societies.findEntity(request.societyId()); Flat flat = destination(request.flatId(), society.getId());
        User operator = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        LogbookEntry entry = new LogbookEntry(); entry.setSociety(society); entry.setDestinationFlat(flat); entry.setEnteredByUser(operator);
        entry.setVisitorName(request.visitorName().trim()); entry.setPhoneNumber(trim(request.phoneNumber())); entry.setVehicleNumber(trim(request.vehicleNumber())); entry.setPurpose(request.purpose().trim());
        return response(entries.save(entry));
    }
    public LogbookEntryResponse exit(Long entryId) {
        LogbookEntry entry = entries.findById(entryId).orElseThrow(() -> new ResourceNotFoundException("Logbook entry", entryId));
        requireOperator(entry.getSociety().getId());
        if (entry.getExitedAt() != null) throw new InvalidRequestException("This visitor has already been checked out");
        entry.setExitedAt(LocalDateTime.now()); entry.setExitedByUser(users.getReferenceById(authorization.currentUserId())); return response(entry);
    }
    @Transactional(readOnly = true) public List<LogbookEntryResponse> active(Long societyId) { requireOperator(societyId); return entries.findBySocietyIdOrderByEnteredAtDesc(societyId).stream().filter(item -> item.getExitedAt() == null).map(this::response).toList(); }
    private void requireOperator(Long societyId) { if (!roles.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(authorization.currentUserId(), societyId, UserRole.LOGBOOK_OPERATOR)) throw new org.springframework.security.access.AccessDeniedException("Logbook operator access is required for this society"); }
    private Flat destination(Long flatId, Long societyId) { if (flatId == null) return null; Flat flat = flats.findById(flatId).orElseThrow(() -> new ResourceNotFoundException("Flat", flatId)); if (!flat.getBlock().getSociety().getId().equals(societyId)) throw new InvalidRequestException("Destination flat must belong to the selected society"); return flat; }
    private String trim(String value) { if (value == null) return null; String result = value.trim(); return result.isBlank() ? null : result; }
    private LogbookEntryResponse response(LogbookEntry item) { return new LogbookEntryResponse(item.getId(), item.getSociety().getId(), item.getDestinationFlat() == null ? null : item.getDestinationFlat().getId(), item.getDestinationFlat() == null ? null : item.getDestinationFlat().getFlatNumber(), item.getVisitorName(), item.getPhoneNumber(), item.getVehicleNumber(), item.getPurpose(), item.getEnteredByUser().getFullName(), item.getExitedByUser() == null ? null : item.getExitedByUser().getFullName(), item.getEnteredAt(), item.getExitedAt()); }
}
