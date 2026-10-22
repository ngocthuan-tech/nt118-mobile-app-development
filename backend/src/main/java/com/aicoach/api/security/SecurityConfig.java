package com.aicoach.api.security;

import com.aicoach.api.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Bảo mật kiểu stateless: không session, không cookie, mỗi request tự mang JWT.
 * Chỉ đăng ký/đăng nhập, health check và Swagger được gọi khi chưa đăng nhập.
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/api/v1/health",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, ObjectMapper mapper)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeError(res, mapper,
                                HttpServletResponse.SC_UNAUTHORIZED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"))
                        .accessDeniedHandler((req, res, e) -> writeError(res, mapper,
                                HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện thao tác này")))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeError(HttpServletResponse res, ObjectMapper mapper, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(res.getOutputStream(), ApiResponse.error(message));
    }
}
