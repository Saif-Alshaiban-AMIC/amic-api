package com.recruitment.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.api.dto.CreateDevRequestPayload;
import com.recruitment.api.dto.DevRequestDto;
import com.recruitment.api.dto.UpdateDevRequestPayload;
import com.recruitment.api.service.DevRequestService;

@RestController
@RequestMapping("/api/dev-requests")
public class DevRequestController {

    private final DevRequestService service;

    public DevRequestController(DevRequestService service) {
        this.service = service;
    }

    @GetMapping
    public List<DevRequestDto> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<DevRequestDto> create(@RequestBody CreateDevRequestPayload payload) {
        return ResponseEntity.ok(service.create(payload));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DevRequestDto> update(@PathVariable Long id,
                                                @RequestBody UpdateDevRequestPayload payload) {
        return ResponseEntity.ok(service.update(id, payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
