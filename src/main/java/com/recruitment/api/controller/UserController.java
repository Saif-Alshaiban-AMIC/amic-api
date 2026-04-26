package com.recruitment.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.api.dto.CreateUserRequest;
import com.recruitment.api.dto.UserDto;
import com.recruitment.api.model.User;
import com.recruitment.api.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDto(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail(),
                        u.getPhoneNumber(),
                        u.getDepartment() != null ? u.getDepartment().name() : null,
                        u.getJobTitle(),
                        u.getRole() != null ? u.getRole().name() : null,
                        u.getCreatedAt()
                ))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {

        if (request.email == null || !request.email.toLowerCase().endsWith("@alkhorayef.com")) {
            return ResponseEntity.badRequest().body(null);
        }

        if (userRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.status(409).build();
        }

        User user = new User();
        user.setFirstName(request.firstName);
        user.setLastName(request.lastName);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setPhoneNumber(request.phoneNumber);
        user.setDepartment(request.department);
        user.setJobTitle(request.jobTitle);
        user.setRole(request.role != null ? request.role : com.recruitment.api.model.Role.USER);

        User saved = userRepository.save(user);

        return ResponseEntity.status(201).body(new UserDto(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                saved.getDepartment() != null ? saved.getDepartment().name() : null,
                saved.getJobTitle(),
                saved.getRole().name(),
                saved.getCreatedAt()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
