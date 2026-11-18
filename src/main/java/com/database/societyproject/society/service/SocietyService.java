package com.database.societyproject.society.service;

import com.database.societyproject.society.entity.Society;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.society.dto.SocietyRequest;
import com.database.societyproject.society.dto.SocietyResponse;
import com.database.societyproject.society.repository.SocietyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SocietyService {
    private final SocietyRepository repository;
    public SocietyService(SocietyRepository repository) { this.repository = repository; }

    public SocietyResponse create(SocietyRequest request) {
        Society society = new Society();
        apply(request, society);
        return toResponse(repository.save(society));
    }

    @Transactional(readOnly = true)
    public List<SocietyResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

    /** Internal scheduling use only; public API access remains authorization-gated. */
    @Transactional(readOnly = true)
    public List<Society> findActiveEntities() { return repository.findByActiveTrue(); }

    @Transactional(readOnly = true)
    public SocietyResponse findById(Long id) { return toResponse(findEntity(id)); }

    public SocietyResponse update(Long id, SocietyRequest request) {
        Society society = findEntity(id);
        apply(request, society);
        return toResponse(society);
    }

    public void deactivate(Long id) { findEntity(id).setActive(false); }

    public Society findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Society", id));
    }

    public Society findEntityForUpdate(Long id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Society", id));
    }

    private void apply(SocietyRequest request, Society society) {
        society.setName(request.name()); society.setAddress(request.address()); society.setCity(request.city());
        society.setState(request.state()); society.setPincode(request.pincode());
        if (request.active() != null) society.setActive(request.active());
    }

    private SocietyResponse toResponse(Society entity) {
        return new SocietyResponse(entity.getId(), entity.getName(), entity.getAddress(), entity.getCity(), entity.getState(),
                entity.getPincode(), entity.getActive(), entity.getCreatedAt());
    }
}
