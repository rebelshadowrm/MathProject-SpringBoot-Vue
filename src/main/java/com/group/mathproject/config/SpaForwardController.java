package com.group.mathproject.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {
    @GetMapping({"/login", "/dashboard", "/courses", "/assignments", "/flashcards", "/drills", "/leaderboard", "/profile", "/test", "/change-password"})
    public String forward() {
        return "forward:/index.html";
    }
}
