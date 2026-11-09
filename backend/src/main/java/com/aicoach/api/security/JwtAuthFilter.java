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
 * đúng phiên bản phiên đăng nhập hiện tại) thì gắn id người dùng vào SecurityContext;
 * không hợp lệ thì bỏ qua — Spring Security sẽ trả 401 ở các API cần đăng nhập.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";
    public static final String REASON_HEADER = "X-Session-Reason";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";

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
                    .filter(info -> stillValid(info, response))
                    .ifPresent(info -> {
                        var auth = new UsernamePasswordAuthenticationToken(info.userId(), null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }

    /** Version check avoids the second-level precision race of JWT iat; old tokens keep the legacy check. */
    private boolean stillValid(JwtService.TokenInfo info, HttpServletResponse response) {
        return users.findById(info.userId())
                .map(u -> {
                    boolean valid = info.sessionVersion() != null
                            ? info.sessionVersion() == u.getSessionVersion()
                            : u.getSessionVersion() == 0 && (u.getPasswordChangedAt() == null
                                || !info.issuedAt().isBefore(u.getPasswordChangedAt()));
                    if (!valid) response.setHeader(REASON_HEADER, PASSWORD_CHANGED);
                    return valid;
                })
                .orElse(false);
    }
}
