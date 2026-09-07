package com.group.mathproject.learning;

import java.time.LocalDateTime;
import java.util.List;

public final class LearningDtos {
    private LearningDtos() {}

    public record Skill(Integer id, String code, String title, String introduction,
                        String workedExample, String hint, String explanation,
                        String masteryStatus, Integer xp) {}
    public record Unit(Integer id, String title, String description, List<Skill> skills) {}
    public record Course(Integer id, Integer grade, String slug, String title,
                         String description, List<Unit> units) {}
    public record SessionRequest(List<String> skillCodes, String mode, Integer questionCount) {}
    public record PracticeQuestion(Integer id, String skillCode, String skillTitle, String prompt,
                                   List<String> answers, String solution, String hint, String explanation) {}
    public record PracticeSession(Integer id, String mode, String status, List<PracticeQuestion> questions) {}
    public record Answer(Integer questionId, String answer) {}
    public record SessionSubmission(List<Answer> answers) {}
    public record Feedback(Integer questionId, String prompt, String selectedAnswer,
                           String correctAnswer, boolean correct, String explanation) {}
    public record SessionResult(Integer score, Integer total, Integer xpEarned, List<Feedback> feedback) {}
    public record Announcement(Integer id, String title, String body, String className,
                               String author, LocalDateTime publishedAt) {}
    public record AssignmentQuestion(Integer id, String skillCode, String prompt, List<String> answers) {}
    public record Assignment(Integer id, String title, String description, String className,
                             LocalDateTime dueAt, boolean completed, Integer score, Integer total,
                             List<AssignmentQuestion> questions) {}
    public record AssignmentSubmission(List<Answer> answers) {}
    public record Progress(String username, Integer totalXp, Integer masteredSkills,
                           Integer proficientSkills, Integer developingSkills, List<Course> courses) {}
    public record LeaderboardEntry(Integer rank, String username, Integer xp, Integer grade) {}
    public record Classroom(Integer id, String name, Integer grade, String teacher,
                            List<String> students) {}
    public record Dashboard(String role, String username, List<Classroom> classes,
                            List<Announcement> announcements, List<Assignment> assignments,
                            List<Progress> learners) {}
    public record CreateClass(String name, Integer grade) {}
    public record CreateStudent(String username, String firstName, String lastName, String email) {}
    public record CreateAnnouncement(String title, String body) {}
    public record CreateAssignment(String title, String description, List<String> skillCodes,
                                   Integer questionCount, LocalDateTime dueAt) {}
    public record LinkParent(String parentUsername) {}
}
