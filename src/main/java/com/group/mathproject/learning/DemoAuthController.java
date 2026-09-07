package com.group.mathproject.learning;

import com.group.mathproject.model.Role;
import com.group.mathproject.model.User;
import com.group.mathproject.security.JwtService;
import com.group.mathproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoAuthController {
    private final UserService users;
    private final JwtService jwt;

    @Value("${app.demo-login.enabled:false}")
    private boolean enabled;

    @PostMapping("/login/{role}")
    public Map<String,String> login(@PathVariable String role, HttpServletRequest request) {
        if (!enabled) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String username = switch (role.toLowerCase(Locale.ROOT)) {
            case "student" -> "Student";
            case "teacher" -> "Teacher";
            case "parent" -> "Parent";
            case "admin" -> "Admin";
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
        User user = users.getUser(username);
        if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return jwt.issueTokens(username, user.getRoles().stream().map(Role::getName).toList(), request.getRequestURL().toString());
    }
}
