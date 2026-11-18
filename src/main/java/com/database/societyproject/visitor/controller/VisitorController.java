package com.database.societyproject.visitor.controller;
import com.database.societyproject.visitor.dto.*;
import com.database.societyproject.visitor.service.VisitorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/visitors") @PreAuthorize("denyAll()") public class VisitorController {
    private final VisitorService service; public VisitorController(VisitorService service) { this.service = service; }
    @PostMapping public ResponseEntity<VisitorResponse> create(@Valid @RequestBody VisitorRequest r) { return ResponseEntity.status(201).body(service.create(r)); }
    @GetMapping public List<VisitorResponse> all() { return service.findAll(); } @GetMapping("/{id}") public VisitorResponse one(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/search") public List<VisitorResponse> search(@RequestParam(required = false) String name, @RequestParam(required = false) String phoneNumber) { return service.search(name, phoneNumber); }
    @PutMapping("/{id}") public VisitorResponse update(@PathVariable Long id, @Valid @RequestBody VisitorRequest r) { return service.update(id, r); }
}
