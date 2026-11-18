package com.database.societyproject.block.service;

import com.database.societyproject.block.dto.BlockRequest;
import com.database.societyproject.block.dto.BlockResponse;
import com.database.societyproject.block.repository.BlockRepository;
import com.database.societyproject.block.entity.Block;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.society.service.SocietyService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlockService {
    private final BlockRepository repository;
    private final SocietyService societyService;
    public BlockService(BlockRepository repository, SocietyService societyService) {
        this.repository = repository;
        this.societyService = societyService;
    }

    public BlockResponse create(BlockRequest request) {
        ensureUnique(request.societyId(), request.name(), null);
        Block block = new Block(); apply(request, block);
        return toResponse(repository.save(block));
    }
    @Transactional(readOnly = true) public List<BlockResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public BlockResponse findById(Long id) {
        return toResponse(findEntity(id)); }
    @Transactional(readOnly = true) public List<BlockResponse> findBySocietyId(Long societyId) {
        societyService.findEntity(societyId); return repository.findBySocietyId(societyId).stream().map(this::toResponse).toList();
    }
    public BlockResponse update(Long id, BlockRequest request) { Block block = findEntity(id); ensureUnique(request.societyId(), request.name(), id); apply(request, block); return toResponse(block); }
    public void deactivate(Long id) { findEntity(id).setActive(false); }
    public Block findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Block", id)); }
    private void ensureUnique(Long societyId, String name, Long id) {
        boolean exists = id == null ? repository.existsBySocietyIdAndName(societyId, name) : repository.existsBySocietyIdAndNameAndIdNot(societyId, name, id);
        if (exists) throw new DuplicateResourceException("A block named '" + name + "' already exists in this society");
    }
    private void apply(BlockRequest request, Block block) {
        Society society = societyService.findEntity(request.societyId());
        block.setSociety(society); block.setName(request.name()); block.setTotalFloors(request.totalFloors()); block.setTotalFlats(request.totalFlats());
        if (request.active() != null) block.setActive(request.active());
    }
    private BlockResponse toResponse(Block entity) { return new BlockResponse(entity.getId(), entity.getName(), entity.getTotalFloors(), entity.getTotalFlats(), entity.getActive(), entity.getCreatedAt(), entity.getSociety().getId()); }
}
