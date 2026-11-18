package com.database.societyproject.flat.service;

import com.database.societyproject.block.service.BlockService;
import com.database.societyproject.block.entity.Block;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.flat.dto.FlatRequest;
import com.database.societyproject.flat.dto.FlatResponse;
import com.database.societyproject.flat.repository.FlatRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FlatService {
    private final FlatRepository repository;
    private final BlockService blockService;
    public FlatService(FlatRepository repository, BlockService blockService) { this.repository = repository; this.blockService = blockService; }
    public FlatResponse create(FlatRequest request) { ensureUnique(request.blockId(), request.flatNumber(), null); Flat flat = new Flat(); apply(request, flat); return toResponse(repository.save(flat)); }
    @Transactional(readOnly = true) public List<FlatResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public FlatResponse findById(Long id) { return toResponse(findEntity(id)); }
    @Transactional(readOnly = true) public List<FlatResponse> findByBlockId(Long blockId) { blockService.findEntity(blockId); return repository.findByBlockId(blockId).stream().map(this::toResponse).toList(); }
    public FlatResponse update(Long id, FlatRequest request) { Flat flat = findEntity(id); ensureUnique(request.blockId(), request.flatNumber(), id); apply(request, flat); return toResponse(flat); }
    public void deactivate(Long id) { findEntity(id).setActive(false); }
    public Flat findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flat", id)); }
    private void ensureUnique(Long blockId, String number, Long id) {
        boolean exists = id == null ? repository.existsByBlockIdAndFlatNumber(blockId, number) : repository.existsByBlockIdAndFlatNumberAndIdNot(blockId, number, id);
        if (exists) throw new DuplicateResourceException("A flat numbered '" + number + "' already exists in this block");
    }
    private void apply(FlatRequest request, Flat flat) {
        Block block = blockService.findEntity(request.blockId());
        flat.setBlock(block); flat.setFlatNumber(request.flatNumber()); flat.setFloorNumber(request.floorNumber()); flat.setFlatType(request.flatType()); flat.setAreaSqft(request.areaSqft());
        if (request.active() != null) flat.setActive(request.active());
    }
    private FlatResponse toResponse(Flat entity) { return new FlatResponse(entity.getId(), entity.getFlatNumber(), entity.getFloorNumber(), entity.getFlatType(), entity.getAreaSqft(), entity.getActive(), entity.getCreatedAt(), entity.getBlock().getId()); }
}
