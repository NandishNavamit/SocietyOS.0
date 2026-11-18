package com.database.societyproject.visitor.controller;
import com.database.societyproject.visitor.dto.*;
import com.database.societyproject.visitor.entity.VisitStatus;
import com.database.societyproject.visitor.service.VisitorVisitService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/visitor-visits") @PreAuthorize("denyAll()") public class VisitorVisitController {
    private final VisitorVisitService service; public VisitorVisitController(VisitorVisitService service) { this.service = service; }
    @PostMapping("/check-in") public ResponseEntity<VisitorVisitResponse> checkIn(@Valid @RequestBody VisitorCheckInRequest r) { return ResponseEntity.status(201).body(service.checkIn(r)); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<VisitorVisitResponse> all() { return service.findAll(); } @GetMapping("/{id}") public VisitorVisitResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/society/{societyId}") public List<VisitorVisitResponse> society(@PathVariable Long societyId) { return service.bySociety(societyId); } @GetMapping("/flat/{flatId}") public List<VisitorVisitResponse> flat(@PathVariable Long flatId) { return service.byFlat(flatId); }
    @GetMapping("/visitor/{visitorId}") @PreAuthorize("hasRole('ADMIN')") public List<VisitorVisitResponse> visitor(@PathVariable Long visitorId) { return service.byVisitor(visitorId); } @GetMapping("/status/{status}") @PreAuthorize("hasRole('ADMIN')") public List<VisitorVisitResponse> status(@PathVariable VisitStatus status) { return service.byStatus(status); }
    @PutMapping("/{id}/check-out") public VisitorVisitResponse checkOut(@PathVariable Long id) { return service.checkOut(id); } @PutMapping("/{id}/deny") public VisitorVisitResponse deny(@PathVariable Long id) { return service.deny(id); }
}
