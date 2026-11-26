package com.aicoach.api.security;

import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Đọc header "Authorization: Bearer &lt;token&gt;". Token hợp lệ (đúng chữ ký, chưa hết hạn, tài khoản còn tồn tại,
 * đúng phiên bản phiên đăng nhập hiện tại) thì gắn id người dùng vào SecurityContext;
 * không hợp lệ thì bỏ qua — Spring Security sẽ trả 401 ở các API cần đăng nhập.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";
    public static final String REASON_HEADER = "X-Session-Reason";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

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
                    .flatMap(info -> currentUser(info, response))
                    .ifPresent(user -> {
                        // Quyền lấy từ cột role trong DB (không nằm trong token) → đổi/thu hồi quyền có hiệu lực ngay
                        var auth = new UsernamePasswordAuthenticationToken(user.getId(), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }

    /**
     * Người dùng của token nếu token còn hiệu lực. Bị khoá → từ chối, kèm lý do ACCOUNT_LOCKED để app báo đúng.
     * Version check avoids the second-level precision race of JWT iat; old tokens keep the legacy check.
     */
    private Optional<UserAccount> currentUser(JwtService.TokenInfo info, HttpServletResponse response) {
        return users.findById(info.userId())
                .filter(u -> {
                    if (u.isLocked()) {
                        response.setHeader(REASON_HEADER, ACCOUNT_LOCKED);
                        return false;
                    }
                    boolean valid = info.sessionVersion() != null
                            ? info.sessionVersion() == u.getSessionVersion()
                            : u.getSessionVersion() == 0 && (u.getPasswordChangedAt() == null
                                || !info.issuedAt().isBefore(u.getPasswordChangedAt()));
                    if (!valid) response.setHeader(REASON_HEADER, PASSWORD_CHANGED);
                    return valid;
                });
    }
}
