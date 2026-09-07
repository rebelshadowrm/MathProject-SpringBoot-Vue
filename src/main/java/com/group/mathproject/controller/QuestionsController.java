package com.group.mathproject.controller;

import com.group.mathproject.exception.NotFoundException;
import com.group.mathproject.model.Question;
import com.group.mathproject.model.QuestionBoolForm;
import com.group.mathproject.model.QuestionForm;
import com.group.mathproject.model.UserQuestion;
import com.group.mathproject.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//yo mama

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionsController {
    private final QuestionService questionService;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions(){
        return ResponseEntity.ok().body(questionService.getAllQuestions());
    }

    @GetMapping("/questions/{num}")
    public List<Question> getSomeQuestions(@PathVariable("num") Integer number){
        return questionService.getSomeQuestions(number);
    }

    @PostMapping("/questions/generate")
    public ResponseEntity<List<Question>> generateQuestions(@RequestBody List<QuestionForm> form) {
        return ResponseEntity.ok()
                .body(questionService.generateQuestions(form));
    }

    @GetMapping("/questions/user/{username}")
    public ResponseEntity<List<UserQuestion>> getQuestionsByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok().body(questionService.getUserQuestionsByUser(username));
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> addQuestion(@RequestBody Question question){
        URI uri = URI.create(ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/questions")
                .toUriString());
        return ResponseEntity.created(uri).body(questionService.saveQuestion(question));
    }


    @PutMapping("/questions/teacher/{id}")
    public ResponseEntity<Question> updateUserQuestionById(
                                    @PathVariable("id") Integer num,
                                    @RequestBody Question question) {
        Optional<UserQuestion> userQuestion = questionService.getUserQuestionById(num);
        questionService.saveQuestion(question);
        userQuestion.orElseThrow().setQuestion(question);
        var timestamp = LocalDateTime.now();
        userQuestion.orElseThrow().setDateTime(timestamp);
        return ResponseEntity.ok().body(questionService
                                  .saveUserQuestion(userQuestion
                                  .orElseThrow()).getQuestion());
    }

    @GetMapping("/questions/by/teacher")
    public ResponseEntity<List<Question>> getTeacherMadeQuestions() {
        return ResponseEntity.ok().body(questionService.getTeacherMadeQuestions());
    }

    @PostMapping("/questions/teacher/{username}")
    public ResponseEntity<Question>saveQuestionToUser(
            @PathVariable("username") String username,
            @RequestBody Question question) {
        URI uri = URI.create(ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/teacher/questions")
                .toUriString());
        var q = questionService.saveQuestionToUser(username, question);
        return ResponseEntity.created(uri).body(q);
    }

    @PostMapping("/questions/{username}")
    public ResponseEntity<List<UserQuestion>>addQuestionsToUser(
            @PathVariable("username") String username,
            @RequestBody List<QuestionBoolForm> qbf) {
        URI uri = URI.create(ServletUriComponentsBuilder
                     .fromCurrentContextPath()
                     .path("/api/questions")
                     .toUriString());
        return ResponseEntity.created(uri)
                .body(questionService.addQuestionsToUser(qbf, username));
    }

    @PutMapping("/questions/{id}")
    public Question updateQuestion(@PathVariable("id") int id,
                               @RequestBody Question newQuestion) {
        Question qu = questionService.findById(id)
                .orElseThrow( () -> new NotFoundException(
                        "Question with id of " + id + " not found!"
                ));
        qu.setQuestion(newQuestion.getQuestion());
        qu.setDifficulty(newQuestion.getDifficulty());
        qu.setSubject(newQuestion.getSubject());
        qu.setAnswer1(newQuestion.getAnswer1());
        qu.setAnswer2(newQuestion.getAnswer2());
        qu.setAnswer3(newQuestion.getAnswer3());
        qu.setAnswer4(newQuestion.getAnswer4());
        qu.setCorrectAnswer(newQuestion.getCorrectAnswer());
        return questionService.saveQuestion(qu);
    }

    @DeleteMapping("/questions/{id}")
    public String deleteQuestion(@PathVariable("id") int id){
        Question qu = questionService.findById(id)
                .orElseThrow( () -> new NotFoundException(
                        "Question with id of " + id + " not found!"
                ));
        questionService.deleteQuestionById(qu.getId());
        return "Question with ID: " + id + " was deleted";
    }

}

