package com.group.mathproject.learning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.mathproject.MathprojectApplication;
import com.group.mathproject.config.PortfolioDemoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioDemoTests {
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();
    private final Map<String, String> tokens = new HashMap<>();
    private String base;

    @Test
    void rejectsSeparateFlywayConnectionsBeforeAnyDatabaseIsOpened() {
        for (String setting : new String[]{"url", "user", "password", "driver-class-name"}) {
            var environment = new MockEnvironment().withProperty("spring.flyway." + setting, "unused");
            assertThatThrownBy(() -> new PortfolioDemoConfiguration().dataSource(environment))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Remove spring.flyway." + setting);
        }
    }

    private ConfigurableApplicationContext start() {
        var context = new SpringApplicationBuilder(MathprojectApplication.class)
                .profiles("portfolio-demo")
                .run("--server.port=0",
                        "--spring.datasource.url=jdbc:postgresql://127.0.0.1:1/must-not-connect",
                        "--spring.datasource.username=unused", "--spring.datasource.password=unused");
        base = "http://localhost:" + ((WebServerApplicationContext) context).getWebServer().getPort();
        tokens.clear();
        return context;
    }

    private JsonNode request(String role, String method, String path, Object body, int status) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(base + path));
        if (role != null) builder.header("Authorization", "Bearer " + tokens.get(role));
        builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(method + " " + path + ": " + response.body()).isEqualTo(status);
        return response.body().isBlank() ? json.nullNode() : json.readTree(response.body());
    }

    @Test
    void allRolesWorkAndRestartRestoresCleanDemoDespiteExternalDatasourceSettings() throws Exception {
        String firstDatabase;
        try (var context = start()) {
            var jdbc = context.getBean(JdbcTemplate.class);
            firstDatabase = jdbc.execute((java.sql.Connection connection) -> connection.getMetaData().getURL());
            assertThat(firstDatabase).startsWith("jdbc:h2:mem:");
            assertThat(jdbc.queryForObject("select count(*) from \"flyway_schema_history\" where \"success\"=true and \"version\" is not null", Integer.class)).isEqualTo(2);
            for (String role : new String[]{"student", "teacher", "parent", "admin"}) {
                tokens.put(role, request(null, "POST", "/api/demo/login/" + role, null, 200).get("access_token").asText());
                assertThat(request(role, "GET", "/api/dashboard", null, 200).get("role").asText()).isEqualTo(role.toUpperCase());
                assertThat(request(role, "GET", "/api/courses", null, 200).size()).isEqualTo(4);
                request(role, "GET", "/api/leaderboards?scope=global", null, 200);
                request(role, "GET", "/api/leaderboards?scope=class", null, 200);
            }
            // Password authentication remains available alongside the role shortcuts.
            var login = http.send(HttpRequest.newBuilder(URI.create(base + "/api/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("username=Student&password=password")).build(), HttpResponse.BodyHandlers.ofString());
            assertThat(login.statusCode()).isEqualTo(200);
            assertThat(json.readTree(login.body()).has("access_token")).isTrue();

            for (String mode : new String[]{"FLASHCARD", "DRILL"}) {
                var session = request("student", "POST", "/api/practice/sessions", Map.of("skillCodes", new String[]{"g4-multi-digit-addition"}, "mode", mode, "questionCount", 3), 201);
                var answers = new java.util.ArrayList<Map<String, Object>>();
                for (var question : session.get("questions")) {
                    String correct = jdbc.queryForObject("select correct_answer from practice_question where id=?", String.class, question.get("id").asInt());
                    answers.add(Map.of("questionId", question.get("id").asInt(), "answer", correct));
                }
                var result = request("student", "POST", "/api/practice/sessions/" + session.get("id").asInt() + "/complete", Map.of("answers", answers), 200);
                assertThat(result.get("score").asInt()).isEqualTo(3);
            }
            var assignment = request("student", "GET", "/api/assignments/me", null, 200).get(0);
            var answers = new java.util.ArrayList<Map<String, Object>>();
            for (var question : assignment.get("questions")) {
                String correct = jdbc.queryForObject("select correct_answer from assessment_question where id=?", String.class, question.get("id").asInt());
                answers.add(Map.of("questionId", question.get("id").asInt(), "answer", correct));
            }
            assertThat(request("student", "POST", "/api/assignments/" + assignment.get("id").asInt() + "/submit", Map.of("answers", answers), 200).get("score").asInt()).isEqualTo(3);
            assertThat(request("parent", "GET", "/api/progress/Student", null, 200).get("totalXp").asInt()).isPositive();
            request("teacher", "GET", "/api/progress/Student", null, 200);
            request("parent", "POST", "/api/classes", Map.of("name", "Forbidden", "grade", 4), 403);
            request("parent", "POST", "/api/practice/sessions", Map.of("skillCodes", new String[]{"g3-division"}, "mode", "DRILL", "questionCount", 3), 403);
            request("student", "GET", "/api/users", null, 403);

            int room = request("teacher", "POST", "/api/classes", Map.of("name", "Temporary demo class", "grade", 4), 201).get("id").asInt();
            var learner = request("teacher", "POST", "/api/classes/" + room + "/students", Map.of("username", "Visitor", "firstName", "Demo", "lastName", "Learner", "email", "visitor@example.com"), 201);
            assertThat(learner.get("temporaryPassword").asText()).isNotBlank();
            request("teacher", "POST", "/api/classes/" + room + "/announcements", Map.of("title", "Temporary news", "body", "Demo update"), 201);
            request("teacher", "POST", "/api/classes/" + room + "/assignments", Map.of("title", "Temporary assessment", "description", "Demo", "skillCodes", new String[]{"g4-multi-digit-addition"}, "questionCount", 3), 201);
            var visitorLogin = http.send(HttpRequest.newBuilder(URI.create(base + "/api/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("username=Visitor&password=" + learner.get("temporaryPassword").asText())).build(), HttpResponse.BodyHandlers.ofString());
            assertThat(visitorLogin.statusCode()).isEqualTo(200);
            tokens.put("visitor", json.readTree(visitorLogin.body()).get("access_token").asText());
            request("visitor", "GET", "/api/courses", null, 403);
            request("visitor", "POST", "/api/account/password", Map.of("currentPassword", learner.get("temporaryPassword").asText(), "newPassword", "fictional-demo-password"), 200);
            var createdAssignment = request("visitor", "GET", "/api/assignments/me", null, 200).get(0);
            assertThat(createdAssignment.get("title").asText()).isEqualTo("Temporary assessment");
            var visitorAnswers = new java.util.ArrayList<Map<String, Object>>();
            for (var question : createdAssignment.get("questions")) {
                String correct = jdbc.queryForObject("select correct_answer from assessment_question where id=?", String.class, question.get("id").asInt());
                visitorAnswers.add(Map.of("questionId", question.get("id").asInt(), "answer", correct));
            }
            assertThat(request("visitor", "POST", "/api/assignments/" + createdAssignment.get("id").asInt() + "/submit", Map.of("answers", visitorAnswers), 200).get("score").asInt()).isEqualTo(3);
            assertThat(request("visitor", "GET", "/api/announcements/me", null, 200).get(0).get("title").asText()).isEqualTo("Temporary news");
            request("teacher", "POST", "/api/learners/Visitor/parents", Map.of("parentUsername", "Parent"), 204);
            assertThat(request("admin", "GET", "/api/users", null, 200).size()).isEqualTo(5);
            request("admin", "POST", "/api/role/addToUser", Map.of("username", "Visitor", "roleName", "ROLE_PARENT"), 200);
            assertThat(request("teacher", "GET", "/api/dashboard", null, 200).get("classes").size()).isEqualTo(2);
        }
        // A fresh context is a new embedded database; no database file is read or removed.
        try (var context = start()) {
            var jdbc = context.getBean(JdbcTemplate.class);
            String nextDatabase = jdbc.execute((java.sql.Connection connection) -> connection.getMetaData().getURL());
            assertThat(nextDatabase).isNotEqualTo(firstDatabase);
            assertThat(jdbc.queryForObject("select count(*) from app_user", Integer.class)).isEqualTo(4);
            assertThat(jdbc.queryForObject("select count(*) from classroom", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from assignment_submission", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from practice_session", Integer.class)).isZero();
            tokens.put("student", request(null, "POST", "/api/demo/login/student", null, 200).get("access_token").asText());
            assertThat(request("student", "GET", "/api/progress/me", null, 200).get("totalXp").asInt()).isZero();
            assertThat(request("student", "GET", "/api/assignments/me", null, 200).get(0).get("completed").asBoolean()).isFalse();
        }
    }
}
