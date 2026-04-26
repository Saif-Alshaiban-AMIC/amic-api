package com.recruitment.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.recruitment.api.dto.BulkCreateResult;
import com.recruitment.api.dto.CreateUserRequest;
import com.recruitment.api.dto.UserDto;
import com.recruitment.api.exception.DuplicateEmailException;
import com.recruitment.api.exception.InvalidDomainException;
import com.recruitment.api.exception.ResourceNotFoundException;
import com.recruitment.api.model.Role;
import com.recruitment.api.model.User;
import com.recruitment.api.repository.UserRepository;

@Service
public class UserService {

    private static final String REQUIRED_DOMAIN = "@alkhorayef.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    public UserDto createUser(CreateUserRequest request) {
        validateDomain(request.email);

        if (userRepository.findByEmail(request.email).isPresent()) {
            throw new DuplicateEmailException(request.email);
        }

        // Generate a 7-day setup token — user must set their own password via email
        String setupToken = UUID.randomUUID().toString();

        User user = new User();
        user.setFirstName(request.firstName);
        user.setLastName(request.lastName);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setPhoneNumber(request.phoneNumber);
        user.setDepartment(request.department);
        user.setJobTitle(request.jobTitle);
        user.setRole(request.role != null ? request.role : Role.USER);
        user.setMustChangePassword(true);
        user.setResetToken(setupToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusDays(7));

        UserDto saved = toDto(userRepository.save(user));

        // Send welcome email (fire-and-forget — don't let email failure block user creation)
        try {
            emailService.sendWelcomeEmail(request.email, request.firstName, setupToken);
        } catch (Exception ignored) {}

        return saved;
    }

    public BulkCreateResult bulkCreate(List<CreateUserRequest> requests) {
        int created = 0;
        List<String> errors = new ArrayList<>();

        for (CreateUserRequest req : requests) {
            try {
                createUser(req);
                created++;
            } catch (InvalidDomainException e) {
                errors.add(req.email + ": invalid domain");
            } catch (DuplicateEmailException e) {
                errors.add(req.email + ": already exists");
            } catch (Exception e) {
                errors.add(req.email + ": unexpected error");
            }
        }

        return new BulkCreateResult(created, errors.size(), errors);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found.");
        }
        userRepository.deleteById(id);
    }

    /**
     * Generates a fresh 1-hour setup token for a user who logs in
     * but still has mustChangePassword = true. Overwrites any existing token.
     */
    public String generateSetupToken(User user) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        return token;
    }

    // ── Password reset ────────────────────────────────────────────────────────

    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendPasswordReset(email, token);
        });
        // Silently succeeds for unknown emails — prevents email enumeration
    }

    public void completePasswordReset(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token."));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateDomain(String email) {
        if (email == null || !email.toLowerCase().endsWith(REQUIRED_DOMAIN)) {
            throw new InvalidDomainException();
        }
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhoneNumber(),
                u.getDepartment() != null ? u.getDepartment().name() : null,
                u.getJobTitle(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getCreatedAt()
        );
    }
}
