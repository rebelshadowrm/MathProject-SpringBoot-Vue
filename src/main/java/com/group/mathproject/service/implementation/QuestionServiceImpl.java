package com.group.mathproject.service.implementation;

import com.group.mathproject.model.QuestionForm;
import com.group.mathproject.model.*;
import com.group.mathproject.repository.QuestionRepository;
import com.group.mathproject.repository.UserQuestionRepository;
import com.group.mathproject.repository.UserRepository;
import com.group.mathproject.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserQuestionRepository userQuestionRepository;

    @Override
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public List<Question> saveQuestions(List<Question> questions){
        return questionRepository.saveAll(questions);
    }

    @Override
    public List<Question> getAllQuestions(){
        //returns all questions in repo
        return questionRepository.findAll();
    }

    @Override
    public List<Question> getSomeQuestions(Integer numOfQuestions){
        //returns only amount of questions specified in default order
        return questionRepository.findAll().subList(0, numOfQuestions - 1);
    }

    @Override
    public List<Question> generateQuestions(List<QuestionForm> forms) {
        List<Question> questions = new ArrayList<>();
        for(QuestionForm form : forms) {
            Question q = new Question();
            q.setRandomQuestion(form.getSubject(), form.getDifficulty());
            questions.add(q);
        }
        return questionRepository.saveAll(questions);
    }

    @Override
    public Optional<Question> findById(Integer id) {
        return questionRepository.findById(id);
    }

    @Override
    public void deleteQuestionById(Integer id) {
        questionRepository.deleteById(id);
    }

    @Override
    public void deleteQuestion(Question question){
        questionRepository.delete(question);
    }

    @Override
    public Question getQuestionBySubject(String subject) {
        return questionRepository.findBySubject(subject);
    }

    @Override
    public List<Question> getQuestionsByUser(String username) {
        var user = userRepository.findByUsername(username);
        var userQuestions = userQuestionRepository.findUserQuestionsByUser(user);
        List<Question> questions = new ArrayList<>();
        for(UserQuestion userQuestion : userQuestions) {
            questions.add(userQuestion.getQuestion());
        }
        return questions;
    }

    @Override
    public List<UserQuestion> getUserQuestionsByUser(String username) {
        var user = userRepository.findByUsername(username);
        return userQuestionRepository.findUserQuestionsByUser(user);
    }

    @Override
    public Optional<UserQuestion> getUserQuestionById(Integer id) {
        return userQuestionRepository.findById(id);
    }

    @Override
    public UserQuestion saveUserQuestion(UserQuestion userQuestion) {
        return userQuestionRepository.save(userQuestion);
    }

    @Override
    public List<Question> getTeacherMadeQuestions() {
        List<UserQuestion> userQuestions = userQuestionRepository.findAll();
        List<Question> questions = new ArrayList<>();
        for(UserQuestion userQuestion : userQuestions ) {
            var roles = userQuestion.getUser().getRoles();
            for(Role role : roles) {
                if(role.getName().equalsIgnoreCase("ROLE_TEACHER")) {
                    questions.add(userQuestion.getQuestion());
                }
            }
        }
        return questions;
    }

    @Override
    public Question saveQuestionToUser(String username, Question question) {
        var user = userRepository.findByUsername(username);
        var userQuestion = new UserQuestion();
        var newQuestion = questionRepository.save(question);
        userQuestion.setUser(user);
        userQuestion.setQuestion(newQuestion);
        return userQuestionRepository.save(userQuestion).getQuestion();
    }

    @Override
    public List<UserQuestion> addQuestionsToUser(List<QuestionBoolForm> qbfs, String username) {
        var user = userRepository.findByUsername(username);
        var userQuestions = new ArrayList<UserQuestion>();
        for(QuestionBoolForm qbf : qbfs) {
            var question = questionRepository.findById(qbf.getQuestionId());
            UserQuestion userQuestion = new UserQuestion();
            userQuestion.setQuestion(question.orElseThrow());
            userQuestion.setUser(user);
            userQuestion.setAnsweredCorrect(qbf.getAnswerCorrect());
            userQuestions.add(userQuestion);
        }
        return userQuestionRepository.saveAll(userQuestions);
    }


}
