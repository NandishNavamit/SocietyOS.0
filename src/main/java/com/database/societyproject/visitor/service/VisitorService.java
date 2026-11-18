package com.database.societyproject.visitor.service;

import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.visitor.dto.VisitorRequest;
import com.database.societyproject.visitor.dto.VisitorResponse;
import com.database.societyproject.visitor.entity.Visitor;
import com.database.societyproject.visitor.repository.VisitorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitorService {
    private final VisitorRepository repository;
    public VisitorService(VisitorRepository repository) { this.repository = repository; }
    public VisitorResponse create(VisitorRequest request) { Visitor visitor = new Visitor(); apply(request, visitor); return toResponse(repository.save(visitor)); }
    @Transactional(readOnly = true) public List<VisitorResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public VisitorResponse findById(Long id) { return toResponse(findEntity(id)); }
    @Transactional(readOnly = true) public List<VisitorResponse> search(String name, String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isBlank()) return repository.findByPhoneNumber(phoneNumber).stream().map(this::toResponse).toList();
        if (name != null && !name.isBlank()) return repository.findByFullNameContainingIgnoreCase(name).stream().map(this::toResponse).toList();
        throw new InvalidRequestException("Provide a name or phoneNumber to search visitors");
    }
    public VisitorResponse update(Long id, VisitorRequest request) { Visitor visitor = findEntity(id); apply(request, visitor); return toResponse(visitor); }
    public Visitor findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Visitor", id)); }
    private void apply(VisitorRequest request, Visitor visitor) { visitor.setFullName(request.fullName()); visitor.setPhoneNumber(request.phoneNumber()); visitor.setVehicleNumber(request.vehicleNumber()); }
    private VisitorResponse toResponse(Visitor visitor) { return new VisitorResponse(visitor.getId(), visitor.getFullName(), visitor.getPhoneNumber(), visitor.getVehicleNumber(), visitor.getCreatedAt()); }
}
