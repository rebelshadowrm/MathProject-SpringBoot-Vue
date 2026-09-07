package com.group.mathproject.service.implementation;

import com.group.mathproject.model.Experience;
import com.group.mathproject.model.Question;
import com.group.mathproject.model.User;
import com.group.mathproject.model.UserQuestion;
import com.group.mathproject.repository.ExperienceRepository;
import com.group.mathproject.repository.QuestionRepository;
import com.group.mathproject.repository.UserQuestionRepository;
import com.group.mathproject.repository.UserRepository;
import com.group.mathproject.service.ExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Transactional
public class ExperienceServiceImpl implements ExperienceService {
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ExperienceRepository experienceRepository;
    private final UserQuestionRepository userQuestionRepository;

    @Override
    public Experience saveExperience(Experience experience) {
        Experience newExperience = new Experience();
        if(!isNull(experience.getId())) {
            newExperience.setId(experience.getId());
        }
        Optional<User> user = userRepository.findById(experience.getUser().getId());
        Experience exp = experienceRepository.findByUserId(user.orElseThrow().getId());
        if(!isNull(exp)) {
            newExperience.setId(exp.getId());
        }
        var userQuestions = userQuestionRepository.findUserQuestionsByUser(newExperience.getUser());
        newExperience.setExp(userQuestions);
        return experienceRepository.save(newExperience);
    }

    @Override
    public void addQuestionToExperience(Integer questionId, Integer experienceId) {
        Optional<Question> qu = questionRepository.findById(questionId);
        Optional<Experience> exp = experienceRepository.findById(experienceId);
        UserQuestion uq = new UserQuestion();
        uq.setUser(exp.orElseThrow().getUser());
        uq.setQuestion(qu.orElseThrow());
        userQuestionRepository.save(uq);
        exp.ifPresent(e -> e.getQuestions().add(qu.orElseThrow()));
    }

    @Override
    public Experience getExperienceByUserId(Integer userId) {
        Experience exp = experienceRepository.findByUserId(userId);
        var userQuestions = userQuestionRepository.findUserQuestionsByUser(exp.getUser());
        exp.setExp(userQuestions);
        return exp;
    }

    @Override
    public Experience getExperienceByUserName(String username) {
        User user = userRepository.findByUsername(username);
        Optional<Experience> exp = Optional.ofNullable(experienceRepository.findByUserId(user.getId()));
        var userQuestions = userQuestionRepository.findUserQuestionsByUser(exp.orElseThrow().getUser());
        exp.orElseThrow().setExp(userQuestions);
        return exp.orElseThrow();
    }

    @Override
    public List<Experience> getExperienceList() {
        List<Experience> experiences = experienceRepository.findAll();
        for(Experience exp : experiences) {
            var userQuestions = userQuestionRepository.findUserQuestionsByUser(exp.getUser());
            exp.setExp(userQuestions);
        }
        return experiences;
    }

}
