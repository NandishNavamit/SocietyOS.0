package com.database.societyproject.visitor.controller;

import com.database.societyproject.flat.dto.FlatResponse;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.society.service.SocietyService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visitor-workspace")
@PreAuthorize("denyAll()")
public class VisitorWorkspaceController {
    private final FlatRepository flats;
    private final SocietyService societies;

    public VisitorWorkspaceController(FlatRepository flats, SocietyService societies) {
        this.flats = flats; this.societies = societies;
    }

    @GetMapping("/societies/{societyId}/flats")
    @PreAuthorize("@societyAuthorization.canAccess(#p0)")
    public List<FlatResponse> flats(@PathVariable Long societyId) {
        societies.findEntity(societyId);
        return flats.findByBlockSocietyIdAndActiveTrue(societyId).stream().map(flat ->
                new FlatResponse(flat.getId(), flat.getFlatNumber(), flat.getFloorNumber(), flat.getFlatType(),
                        flat.getAreaSqft(), flat.getActive(), flat.getCreatedAt(), flat.getBlock().getId())).toList();
    }
}
