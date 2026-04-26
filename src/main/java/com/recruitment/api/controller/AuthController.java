package com.recruitment.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recruitment.api.dto.AuthResponse;
import com.recruitment.api.dto.LoginRequest;
import com.recruitment.api.dto.RefreshRequest;
import com.recruitment.api.model.RefreshToken;
import com.recruitment.api.model.User;
import com.recruitment.api.security.JwtService;
import com.recruitment.api.security.LoginRateLimiter;
import com.recruitment.api.service.RefreshTokenService;
import com.recruitment.api.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final long ACCESS_TOKEN_EXPIRES_IN = 15 * 60; // seconds

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          LoginRateLimiter rateLimiter) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        String ip = LoginRateLimiter.getClientIp(httpRequest);

        if (!rateLimiter.isAllowed(ip)) {
            return ResponseEntity.status(429).build();
        }

        User user = userService.findByEmail(request.email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password, user.getPassword())) {
            rateLimiter.recordFailure(ip);
            return ResponseEntity.status(401).build();
        }

        rateLimiter.recordSuccess(ip);

        // First-login guard — user must set their own password before receiving tokens
        if (user.isMustChangePassword()) {
            String setupToken = userService.generateSetupToken(user);
            return ResponseEntity.ok(AuthResponse.requiresSetup(setupToken));
        }

        String accessToken      = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String rawRefreshToken  = refreshTokenService.createRefreshToken(user);

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
            RefreshToken old  = refreshTokenService.validateAndRotate(request.refreshToken);
            User user         = old.getUser();
            String accessToken     = jwtService.generateToken(user.getEmail(), user.getRole().name());
            String newRefreshToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(new AuthResponse(
                    accessToken,
                    newRefreshToken,
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
}
