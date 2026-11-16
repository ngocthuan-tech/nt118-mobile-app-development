package com.aicoach.api.security;

import io.jsonwebtoken.Claims;
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

    public String generate(String userId, long sessionVersion) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId)
                .id(java.util.UUID.randomUUID().toString())
                .claim("sv", sessionVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** Verified claims; null sessionVersion identifies a token issued before the V4 rollout. */
    public record TokenInfo(String userId, Instant issuedAt, Long sessionVersion) {
    }

    /** Trả về id người dùng + thời điểm cấp nếu token hợp lệ và chưa hết hạn. */
    public Optional<TokenInfo> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getSubject() == null || claims.getIssuedAt() == null) {
                return Optional.empty();
            }
            Long version = claims.get("sv", Long.class);
            if (version != null && version < 0) return Optional.empty();
            return Optional.of(new TokenInfo(claims.getSubject(), claims.getIssuedAt().toInstant(), version));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<String> parseUserId(String token) {
        return parse(token).map(TokenInfo::userId);
    }

    public long ttlSeconds() {
        return ttl.toSeconds();
    }
}
