package com.recruitment.api.security;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 10 * 60 * 1000L;   // 10 minutes
    private static final long LOCKOUT_MS = 10 * 60 * 1000L;  // 10 minutes

    private record AttemptRecord(int count, Instant windowStart, Instant lockedUntil) {}

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        Instant now = Instant.now();
        AttemptRecord record = attempts.get(ip);

        if (record == null) return true;

        if (record.lockedUntil() != null && now.isBefore(record.lockedUntil())) {
            return false;
        }

        if (now.toEpochMilli() - record.windowStart().toEpochMilli() > WINDOW_MS) {
            attempts.remove(ip);
            return true;
        }

        return record.count() < MAX_ATTEMPTS;
    }

    public void recordFailure(String ip) {
        Instant now = Instant.now();
        attempts.compute(ip, (key, existing) -> {
            if (existing == null || now.toEpochMilli() - existing.windowStart().toEpochMilli() > WINDOW_MS) {
                return new AttemptRecord(1, now, null);
            }
            int newCount = existing.count() + 1;
            Instant lockout = newCount >= MAX_ATTEMPTS ? now.plusMillis(LOCKOUT_MS) : null;
            return new AttemptRecord(newCount, existing.windowStart(), lockout);
        });
    }

    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }

    public static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void cleanup() {
        Instant cutoff = Instant.now().minusMillis(WINDOW_MS * 2);
        Iterator<Map.Entry<String, AttemptRecord>> it = attempts.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().windowStart().isBefore(cutoff)) {
                it.remove();
            }
        }
    }
}
