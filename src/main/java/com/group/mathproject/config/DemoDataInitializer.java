package com.group.mathproject.config;

import com.group.mathproject.model.Question;
import com.group.mathproject.model.Role;
import com.group.mathproject.model.User;
import com.group.mathproject.repository.QuestionRepository;
import com.group.mathproject.repository.RoleRepository;
import com.group.mathproject.service.UserService;
import com.group.mathproject.learning.LearningSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private static final List<String> ROLES = List.of(
            "ROLE_STUDENT", "ROLE_PARENT", "ROLE_TEACHER", "ROLE_ADMIN"
    );
    private static final List<String> SUBJECTS = List.of(
            "ADDITION", "SUBTRACTION", "MULTIPLICATION", "DIVISION"
    );

    private final RoleRepository roleRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;
    private final LearningSeedService learningSeedService;

    @Override
    public void run(ApplicationArguments args) {
        ROLES.stream()
                .filter(role -> roleRepository.findByName(role) == null)
                .map(role -> new Role(null, role))
                .forEach(roleRepository::save);

        createUser("Admin", "Admin", "User", "ROLE_ADMIN");
        createUser("Student", "Student", "User", "ROLE_STUDENT");
        createUser("Teacher", "Teacher", "User", "ROLE_TEACHER");
        createUser("Parent", "Parent", "User", "ROLE_PARENT");

        if (questionRepository.count() == 0) {
            SUBJECTS.forEach(subject -> {
                for (int difficulty = 1; difficulty <= 3; difficulty++) {
                    Question question = new Question();
                    question.setRandomQuestion(subject, difficulty);
                    questionRepository.save(question);
                }
            });
        }
        learningSeedService.seed();
    }

    private void createUser(String username, String firstName, String lastName, String role) {
        if (userService.getUser(username) != null) {
            return;
        }

        userService.saveUser(new User(
                null,
                username,
                "password",
                username.toLowerCase() + "@example.com",
                firstName,
                lastName,
                new ArrayList<>()
        ));
        userService.addRoleToUser(username, role);
    }
}
