package com.database.societyproject.logbook.controller;

import com.database.societyproject.logbook.dto.*;
import com.database.societyproject.logbook.service.PrivateLogbookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/private-logbook")
@PreAuthorize("hasRole('LOGBOOK_OPERATOR')")
public class PrivateLogbookController {
    private final PrivateLogbookService service;
    public PrivateLogbookController(PrivateLogbookService service) { this.service = service; }
    @GetMapping("/my-societies") public List<LogbookSocietyResponse> mySocieties() { return service.mySocieties(); }
    @GetMapping("/societies/{societyId}/entries") public List<LogbookEntryResponse> entries(@PathVariable Long societyId) { return service.entries(societyId); }
    @GetMapping("/societies/{societyId}/flats") public List<LogbookFlatResponse> flats(@PathVariable Long societyId) { return service.flats(societyId); }
    @GetMapping("/societies/{societyId}/active") public List<LogbookEntryResponse> active(@PathVariable Long societyId) { return service.active(societyId); }
    @PostMapping("/entries") public LogbookEntryResponse enter(@Valid @RequestBody LogbookEntryCreateRequest request) { return service.enter(request); }
    @PostMapping("/entries/{entryId}/exit") public LogbookEntryResponse exit(@PathVariable Long entryId) { return service.exit(entryId); }
}
