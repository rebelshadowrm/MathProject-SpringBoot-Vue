package com.group.mathproject.service.implementation;

import com.group.mathproject.controller.QuestionBoolForm;
import com.group.mathproject.model.Question;
import com.group.mathproject.model.UserQuestion;
import com.group.mathproject.repository.QuestionRepository;
import com.group.mathproject.repository.UserQuestionRepository;
import com.group.mathproject.repository.UserRepository;
import com.group.mathproject.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Array;
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
    public List<Question> getSomeQuestions(int numOfQuestions){
        //returns only amount of questions specified in default order
        return questionRepository.findAll().subList(0, numOfQuestions - 1);
    }

    @Override
    public List<Question> generateQuestions(Integer number, String Subject, Integer Difficulty) {
        List<Question> questions = new ArrayList<>();
        for(int i = 0; i < number; i++) {
            Question q = new Question();
            q.setRandomQuestion(Subject, Difficulty);
            questions.add(q);
        }
        return questionRepository.saveAll(questions);
    }

    @Override
    public Optional<Question> findById(int id) {
        return questionRepository.findById(id);
    }

    @Override
    public void deleteQuestionById(int id) {
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
    public List<UserQuestion> addQuestionToUser(List<QuestionBoolForm> qbfs, String username) {
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