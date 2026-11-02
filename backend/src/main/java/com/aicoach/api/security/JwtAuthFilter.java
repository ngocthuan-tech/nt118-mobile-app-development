package com.aicoach.api.security;

import com.aicoach.api.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Đọc header "Authorization: Bearer &lt;token&gt;". Token hợp lệ (đúng chữ ký, chưa hết hạn, tài khoản còn tồn tại,
 * cấp sau lần đặt lại mật khẩu gần nhất) thì gắn id người dùng vào SecurityContext;
 * không hợp lệ thì bỏ qua — Spring Security sẽ trả 401 ở các API cần đăng nhập.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIX)) {
            jwtService.parse(header.substring(PREFIX.length()).trim())
                    .filter(this::stillValid)
                    .ifPresent(info -> {
                        var auth = new UsernamePasswordAuthenticationToken(info.userId(), null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }

    /** Token cấp trước lần đặt lại mật khẩu gần nhất thì không còn hiệu lực (đăng xuất mọi thiết bị cũ). */
    private boolean stillValid(JwtService.TokenInfo info) {
        return users.findById(info.userId())
                .map(u -> u.getPasswordChangedAt() == null || !info.issuedAt().isBefore(u.getPasswordChangedAt()))
                .orElse(false);
    }
}
