package com.group.mathproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public record PasswordChange(String currentPassword, String newPassword) {}

    @GetMapping("/status")
    public Map<String, Boolean> status(Principal principal) {
        Boolean required = jdbc.queryForObject(
                "select password_change_required from app_user where username=?", Boolean.class, principal.getName());
        return Map.of("passwordChangeRequired", Boolean.TRUE.equals(required));
    }

    @PostMapping("/password")
    public Map<String, Boolean> changePassword(@RequestBody PasswordChange request, Principal principal) {
        if (request.newPassword() == null || request.newPassword().length() < 10)
            throw new IllegalArgumentException("New password must be at least 10 characters");
        String hash = jdbc.queryForObject("select password from app_user where username=?", String.class, principal.getName());
        if (!passwordEncoder.matches(request.currentPassword(), hash))
            throw new IllegalArgumentException("Current password is incorrect");
        jdbc.update("update app_user set password=?,password_change_required=false where username=?",
                passwordEncoder.encode(request.newPassword()), principal.getName());
        return Map.of("passwordChangeRequired", false);
    }
}
