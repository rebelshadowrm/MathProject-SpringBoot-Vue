package com.group.mathproject.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private Integer currentExp = 0;
    private Integer expPerLevel = 20;
    private Integer level = 0;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(cascade = CascadeType.ALL)
    private Set<Question> questions;


    public void setExp(List<UserQuestion> uqs) {
        currentExp = 0;
        for(UserQuestion uq : uqs) {
            user = uq.getUser();
            Question q = uq.getQuestion();
            questions.add(q);
            if(q.getDifficulty() != null) {
                if(uq.isAnsweredCorrect()) {
                    currentExp += q.getDifficulty();
                }
            }
        }
        expPerLevel = 20;
        level = (currentExp / expPerLevel);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "currentExp = " + currentExp + ", " +
                "expPerLevel = " + expPerLevel + ", " +
                "level = " + level + ", " +
                "user = " + user + ")";
    }
}
