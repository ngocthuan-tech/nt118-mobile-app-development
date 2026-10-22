package com.aicoach.api.auth;

import com.aicoach.api.auth.AuthDtos.AuthResponse;
import com.aicoach.api.auth.AuthDtos.LoginRequest;
import com.aicoach.api.auth.AuthDtos.RegisterRequest;
import com.aicoach.api.common.ApiException;
import com.aicoach.api.security.JwtService;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import com.aicoach.api.user.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private static final String WRONG_CREDENTIALS = "Email hoặc mật khẩu không đúng";

    private final UserRepository users;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Hash giả để thời gian phản hồi như nhau dù email có tồn tại hay không (chống dò email). */
    private final String dummyHash;

    public AuthService(UserRepository users, UserService userService,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("dummy-password-1");
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Email này đã được đăng ký");
        }
        UserAccount u = new UserAccount();
        u.setEmail(email);
        u.setDisplayName(req.displayName().trim());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        try {
            users.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("Email này đã được đăng ký");
        }
        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Optional<UserAccount> found = users.findByEmail(normalizeEmail(req.email()));
        if (found.isEmpty()) {
            passwordEncoder.matches(req.password(), dummyHash);
            throw ApiException.unauthorized(WRONG_CREDENTIALS);
        }
        UserAccount u = found.get();
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized(WRONG_CREDENTIALS);
        }
        return toResponse(u);
    }

    private AuthResponse toResponse(UserAccount u) {
        return new AuthResponse(jwtService.generate(u.getId()), "Bearer", jwtService.ttlSeconds(),
                userService.toDto(u));
    }
}
