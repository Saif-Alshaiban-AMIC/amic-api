package com.recruitment.api.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.api.dto.ProfileUpdateRequest;
import com.recruitment.api.dto.UserProfileResponse;
import com.recruitment.api.model.User;
import com.recruitment.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;





@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User currentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        User user = currentUser();
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody ProfileUpdateRequest request) {
        User user = currentUser();

        if (request.getFirstName()  != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()   != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber()!= null) user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .department(user.getDepartment() != null
                        ? user.getDepartment().name() : null)
                .jobTitle(user.getJobTitle())
                .profilePicture(user.getProfilePicture())
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().toLocalDate().toString() : null)
                .build();
    }
}