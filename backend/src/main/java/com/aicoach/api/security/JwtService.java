package com.aicoach.api.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Cấp và kiểm tra JWT (HS256). Subject của token là id người dùng. */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-days:7}") long expirationDays,
                      Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET phải có ít nhất 32 ký tự. Tạo bằng lệnh: openssl rand -base64 48");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofDays(expirationDays);
        this.clock = clock;
    }

    public String generate(String userId) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** Trả về id người dùng nếu token hợp lệ và chưa hết hạn. */
    public Optional<String> parseUserId(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(subject);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long ttlSeconds() {
        return ttl.toSeconds();
    }
}
