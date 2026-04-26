package com.recruitment.api.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.api.dto.AuthResponse;
import com.recruitment.api.dto.LoginRequest;
import com.recruitment.api.dto.RefreshRequest;
import com.recruitment.api.dto.RegisterRequest;
import com.recruitment.api.model.RefreshToken;
import com.recruitment.api.model.Role;
import com.recruitment.api.model.User;
import com.recruitment.api.repository.UserRepository;
import com.recruitment.api.security.JwtService;
import com.recruitment.api.security.LoginRateLimiter;
import com.recruitment.api.service.EmailService;
import com.recruitment.api.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final long ACCESS_TOKEN_EXPIRES_IN = 15 * 60; // seconds

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimiter rateLimiter;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            LoginRateLimiter rateLimiter,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.rateLimiter = rateLimiter;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.status(409).body("An account with this email already exists.");
        }

        User user = new User();
        user.setFirstName(request.firstName);
        user.setLastName(request.lastName);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setPhoneNumber(request.phoneNumber);
        user.setDepartment(request.department);
        user.setJobTitle(request.jobTitle);
        user.setRole(Role.USER);

        userRepository.save(user);

        return ResponseEntity.status(201).body("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = LoginRateLimiter.getClientIp(httpRequest);

        if (!rateLimiter.isAllowed(ip)) {
            return ResponseEntity.status(429).build();
        }

        User user = userRepository.findByEmail(request.email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password, user.getPassword())) {
            rateLimiter.recordFailure(ip);
            return ResponseEntity.status(401).build();
        }

        rateLimiter.recordSuccess(ip);

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                rawRefreshToken,
                ACCESS_TOKEN_EXPIRES_IN,
                user.getRole().name()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            RefreshToken old = refreshTokenService.validateAndRotate(request.refreshToken);
            User user = old.getUser();

            String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
            String newRawRefreshToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(new AuthResponse(
                    accessToken,
                    newRawRefreshToken,
                    ACCESS_TOKEN_EXPIRES_IN,
                    user.getRole().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        try {
            RefreshToken stored = refreshTokenService.validateAndRotate(request.refreshToken);
            refreshTokenService.revokeAllForUser(stored.getUser());
        } catch (RuntimeException ignored) {
            // Always succeed on logout even if token is already invalid
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {

        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendPasswordReset(email, token);
        });

        return "If that email exists, a reset link has been sent";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
            @RequestParam String newPassword) {

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return "Token expired";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        return "Password updated successfully";
    }
}
