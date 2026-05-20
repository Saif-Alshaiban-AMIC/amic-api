package com.recruitment.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.recruitment.api.dto.CreateDevRequestPayload;
import com.recruitment.api.dto.DevRequestDto;
import com.recruitment.api.dto.UpdateDevRequestPayload;
import com.recruitment.api.exception.ResourceNotFoundException;
import com.recruitment.api.model.DevRequest;
import com.recruitment.api.repository.DevRequestRepository;

@Service
public class DevRequestService {

    private final DevRequestRepository repo;

    public DevRequestService(DevRequestRepository repo) {
        this.repo = repo;
    }

    public List<DevRequestDto> getAll() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public DevRequestDto create(CreateDevRequestPayload p) {
        DevRequest r = new DevRequest();
        r.setRequesterName(p.requesterName);
        r.setRequesterEmail(p.requesterEmail);
        r.setDepartment(p.department);
        r.setAppName(p.appName);
        r.setAppType(p.appType);
        r.setDescription(p.description);
        r.setPriority(p.priority);
        r.setTargetDate(p.targetDate);
        r.setStatus("PENDING");
        return toDto(repo.save(r));
    }

    public DevRequestDto update(Long id, UpdateDevRequestPayload p) {
        DevRequest r = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dev request " + id + " not found"));
        if (p.status != null) r.setStatus(p.status);
        if (p.notes  != null) r.setNotes(p.notes);
        return toDto(repo.save(r));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Dev request " + id + " not found");
        }
        repo.deleteById(id);
    }

    private DevRequestDto toDto(DevRequest r) {
        return new DevRequestDto(
            r.getId(), r.getRequesterName(), r.getRequesterEmail(),
            r.getDepartment(), r.getAppName(), r.getAppType(),
            r.getDescription(), r.getPriority(), r.getTargetDate(),
            r.getStatus(), r.getNotes(), r.getCreatedAt()
        );
    }
}
