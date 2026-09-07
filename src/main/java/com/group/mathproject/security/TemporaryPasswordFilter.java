package com.group.mathproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TemporaryPasswordFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String path = request.getRequestURI();
        if (auth != null && auth.isAuthenticated() && path.startsWith("/api/") && !path.startsWith("/api/account/")
                && !path.startsWith("/api/login") && !path.startsWith("/api/token/") && !path.startsWith("/api/demo/login")) {
            Integer required = jdbc.queryForObject(
                    "select count(*) from app_user where username=? and password_change_required=true",
                    Integer.class, auth.getName());
            if (required != null && required > 0) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Password change required\",\"code\":\"PASSWORD_CHANGE_REQUIRED\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
