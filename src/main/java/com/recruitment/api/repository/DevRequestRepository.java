package com.recruitment.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitment.api.model.DevRequest;

public interface DevRequestRepository extends JpaRepository<DevRequest, Long> {
    List<DevRequest> findAllByOrderByCreatedAtDesc();
}
