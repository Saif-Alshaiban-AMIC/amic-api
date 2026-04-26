package com.recruitment.api.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recruitment.api.model.RefreshToken;
import com.recruitment.api.model.User;
import com.recruitment.api.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hash(rawToken);

        RefreshToken entity = new RefreshToken();
        entity.setToken(hashedToken);
        entity.setUser(user);
        entity.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS));

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Transactional
    public RefreshToken validateAndRotate(String rawToken) {
        String hashedToken = hash(rawToken);

        RefreshToken stored = refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.isRevoked()) {
            // Possible token reuse — revoke all tokens for this user
            refreshTokenRepository.deleteByUser(stored.getUser());
            throw new RuntimeException("Refresh token reuse detected");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new RuntimeException("Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return stored;
    }

    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
