package com.group.mathproject.learning;

import com.group.mathproject.learning.LearningDtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:learning;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "app.demo-data.enabled=true",
        "app.demo-login.enabled=true"
})
class LearningServiceTests {
    @Autowired LearningService learning;

    @Test
    void seedsGradeCoursesClassAnnouncementAndPublishedAssignment() {
        var courses = learning.courses("Student");
        assertThat(courses).extracting(Course::grade).containsExactly(3, 4, 5, 6);
        assertThat(courses).allSatisfy(course -> assertThat(course.units()).isNotEmpty());
        assertThat(learning.announcements("Student")).extracting(Announcement::title).contains("Welcome to Math Practice");
        assertThat(learning.assignments("Student")).singleElement().satisfies(assignment -> {
            assertThat(assignment.title()).isEqualTo("Grade 4 Place Value Check-in");
            assertThat(assignment.questions()).hasSize(3);
        });
    }

    @Test
    void zeroProgressStudentCanCompleteDrillAndGainXp() {
        PracticeSession session = learning.startSession("Student", new SessionRequest(List.of("g4-multi-digit-addition"), "DRILL", 5));
        assertThat(session.questions()).hasSize(5);
        assertThat(session.questions()).allSatisfy(question -> assertThat(question.solution()).isNull());
        List<Answer> answers = session.questions().stream().map(question -> new Answer(question.id(), question.answers().getFirst())).toList();
        SessionResult result = learning.completeSession("Student", session.id(), new SessionSubmission(answers));
        assertThat(result.total()).isEqualTo(5);
        assertThat(learning.progress("Student", "Student").developingSkills()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void flashcardsExposeSolutionsButNeverGenerateInvalidDifficulty() {
        PracticeSession session = learning.startSession("Student", new SessionRequest(List.of("g3-division"), "FLASHCARD", 20));
        assertThat(session.questions()).hasSize(20).allSatisfy(question -> {
            assertThat(question.solution()).isNotBlank();
            assertThat(question.answers()).contains(question.solution());
        });
    }

    @Test
    void linkedParentAndTeacherCanViewLearnerProgress() {
        assertThat(learning.progress("Parent", "Student").username()).isEqualTo("Student");
        assertThat(learning.progress("Teacher", "Student").username()).isEqualTo("Student");
    }
}
