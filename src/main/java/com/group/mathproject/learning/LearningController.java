package com.group.mathproject.learning;

import com.group.mathproject.learning.LearningDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LearningController {
    private final LearningService learning;

    @GetMapping("/courses")
    public List<Course> courses(Principal principal) { return learning.courses(principal.getName()); }

    @PostMapping("/practice/sessions")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT','ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public PracticeSession start(@RequestBody SessionRequest request, Principal principal) {
        return learning.startSession(principal.getName(), request);
    }

    @PostMapping("/practice/sessions/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT','ROLE_ADMIN')")
    public SessionResult complete(@PathVariable Integer id, @RequestBody SessionSubmission request, Principal principal) {
        return learning.completeSession(principal.getName(), id, request);
    }

    @GetMapping("/assignments/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public List<Assignment> assignments(Principal principal) { return learning.assignments(principal.getName()); }

    @PostMapping("/assignments/{id}/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public SessionResult submitAssignment(@PathVariable Integer id, @RequestBody AssignmentSubmission request, Principal principal) {
        return learning.submitAssignment(principal.getName(), id, request);
    }

    @GetMapping("/progress/me")
    public Progress myProgress(Principal principal) { return learning.progress(principal.getName(), principal.getName()); }

    @GetMapping("/progress/{username}")
    public Progress progress(@PathVariable String username, Principal principal) { return learning.progress(principal.getName(), username); }

    @GetMapping("/leaderboards")
    public List<LeaderboardEntry> leaderboard(@RequestParam(defaultValue="class") String scope,
                                               @RequestParam(required=false) Integer classId, Principal principal) {
        return learning.leaderboard(principal.getName(), scope, classId);
    }

    @GetMapping("/announcements/me")
    public List<Announcement> announcements(Principal principal) { return learning.announcements(principal.getName()); }

    @GetMapping("/dashboard")
    public Dashboard dashboard(Authentication authentication) { return learning.dashboard(authentication); }

    @PostMapping("/classes")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER','ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Classroom createClass(@RequestBody CreateClass request, Principal principal) { return learning.createClass(principal.getName(), request); }

    @PostMapping("/classes/{id}/students")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> createStudent(@PathVariable Integer id, @RequestBody CreateStudent request, Principal principal) {
        return learning.createStudent(principal.getName(), id, request);
    }

    @PostMapping("/classes/{id}/announcements")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Announcement createAnnouncement(@PathVariable Integer id, @RequestBody CreateAnnouncement request, Principal principal) {
        return learning.createAnnouncement(principal.getName(), id, request);
    }

    @PostMapping("/classes/{id}/assignments")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Assignment createAssignment(@PathVariable Integer id, @RequestBody CreateAssignment request, Principal principal) {
        return learning.createAssignment(principal.getName(), id, request);
    }

    @PostMapping("/learners/{student}/parents")
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER','ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkParent(@PathVariable String student, @RequestBody LinkParent request, Principal principal) {
        learning.linkParent(principal.getName(), student, request.parentUsername());
    }
}
