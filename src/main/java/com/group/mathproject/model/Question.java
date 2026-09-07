package com.group.mathproject.model;

import lombok.*;
import org.hibernate.Hibernate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;
    private String question;
    //Couldn't get the ORM to work properly with these as ENUM types.
    private String subject;
    private Integer difficulty;
    //Couldn't get the ORM to work properly with this as an individual object.
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    private Integer correctAnswer;


    public void setRandomQuestion(String sub, Integer diff) {
        Question qu = generateQuestion(sub, diff);
        id = null;
        question = qu.getQuestion();
        subject = sub;
        difficulty = diff;
        answer1 = qu.getAnswer1();
        answer2 = qu.getAnswer2();
        answer3 = qu.getAnswer3();
        answer4 = qu.getAnswer4();
        correctAnswer = qu.getCorrectAnswer();
    }

    private Question generateQuestion(String Subject, Integer difficulty) {
        return switch (Subject) {
            case "ADDITION" -> additionProblem(Subject, difficulty);
            case "SUBTRACTION" -> subtractionProblem(Subject, difficulty);
            case "MULTIPLICATION" -> multiplicationProblem(Subject, difficulty);
            case "DIVISION" -> divisionProblem(Subject, difficulty);
            default -> throw new IllegalArgumentException("Invalid Subject type: " + Subject);
        };
    }

    private Integer getRandom(Integer difficulty) {
        Random rand = new Random();
        return switch (difficulty) {
            case 1 -> rand.nextInt(10, 99);
            case 2 -> rand.nextInt(50,999);
            case 3 -> rand.nextInt(90, 9999);
            default -> throw new IllegalArgumentException("Invalid difficulty level: " + difficulty);
        };
    }

    private List<String> answerRandomizer(Integer questionAnswer, Integer q) {
        List<String> answers = new ArrayList<>();
        switch (q) {
            case 1 -> {
                correctAnswer = 1;
                if (questionAnswer > 30) {
                    answers.add(String.valueOf(questionAnswer));
                    answers.add(String.valueOf(questionAnswer - 10));
                    answers.add(String.valueOf(questionAnswer - 20));
                    answers.add(String.valueOf(questionAnswer + 10));
                } else {
                    answers.add(String.valueOf(questionAnswer));
                    answers.add(String.valueOf(questionAnswer + 5));
                    answers.add(String.valueOf(questionAnswer + 10));
                    answers.add(String.valueOf(questionAnswer + 15));
                }
            }
            case 2 -> {
                correctAnswer = 2;
                if (questionAnswer > 30) {
                    answers.add(String.valueOf(questionAnswer - 10));
                    answers.add(String.valueOf(questionAnswer));
                    answers.add(String.valueOf(questionAnswer - 20));
                    answers.add(String.valueOf(questionAnswer + 10));
                } else {
                    answers.add(String.valueOf(questionAnswer + 5));
                    answers.add(String.valueOf(questionAnswer));
                    answers.add(String.valueOf(questionAnswer + 10));
                    answers.add(String.valueOf(questionAnswer + 15));
                }
            }
            case 3 -> {
                correctAnswer = 3;
                if (questionAnswer > 30) {
                    answers.add(String.valueOf(questionAnswer - 20));
                    answers.add(String.valueOf(questionAnswer - 10));
                    answers.add(String.valueOf(questionAnswer));
                    answers.add(String.valueOf(questionAnswer + 10));
                } else {
                    answers.add(String.valueOf(questionAnswer + 10));
                    answers.add(String.valueOf(questionAnswer + 5));
                    answers.add(String.valueOf(questionAnswer));
                    answers.add(String.valueOf(questionAnswer + 15));
                }
            }
            case 4 -> {
                correctAnswer = 3;
                if (questionAnswer > 30) {
                    answers.add(String.valueOf(questionAnswer + 10));
                    answers.add(String.valueOf(questionAnswer - 10));
                    answers.add(String.valueOf(questionAnswer - 20));
                    answers.add(String.valueOf(questionAnswer));
                } else {
                    answers.add(String.valueOf(questionAnswer + 10));
                    answers.add(String.valueOf(questionAnswer + 5));
                    answers.add(String.valueOf(questionAnswer + 10));
                    answers.add(String.valueOf(questionAnswer));
                }
            }
            default -> throw new IllegalArgumentException("Invalid answer #: " + q);
        }
        return answers;
    }

    private Question additionProblem(String subject, Integer difficulty) {
        String question, a1, a2, a3, a4;
        int correctAnswer;
        int questionAnswer;
        Random rand = new Random();
        int q = rand.nextInt(1,5);
        var v1 = getRandom(difficulty);
        var v2 = getRandom(difficulty);
        var v3 = getRandom(difficulty);
        var v4 = getRandom(difficulty);

        switch(difficulty) {
            case 1 -> {
                questionAnswer = v1 + v2;
                question = v1 + " + " + v2;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 2 -> {
                questionAnswer = v1 + v2 + v3;
                question = v1 + " + " + v2 + " + " + v3;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 3 -> {
                questionAnswer = v1 + v2  + v3  + v4;
                question = v1 + " + " + v2 + " + " + v3 + " + " + v4;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            default -> throw new IllegalArgumentException("Invalid difficulty level: " + difficulty);
        }
        return new Question(null, question, subject, difficulty, a1, a2, a3, a4, correctAnswer);
    }

    private Question subtractionProblem(String subject, Integer difficulty) {
        String question, a1, a2, a3, a4;
        int correctAnswer;
        int questionAnswer;
        Random rand = new Random();
        int q = rand.nextInt(1,5);
        var v1 = getRandom(difficulty);
        var v2 = getRandom(difficulty);
        var v3 = getRandom(difficulty);
        var v4 = getRandom(difficulty);

        switch(difficulty) {
            case 1 -> {
                questionAnswer = v1 - v2;
                question = v1 + " - " + v2;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 2 -> {
                questionAnswer = v1 + v2 - v3;
                question = v1 + " + " + v2 + " - " + v3;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 3 -> {
                questionAnswer = v1 + v2 - v3 - v4;
                question = v1 + " + " + v2 + " - " + v3 + " - " + v4;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            default -> throw new IllegalArgumentException("Invalid difficulty level: " + difficulty);
        }
        return new Question(null, question, subject, difficulty, a1, a2, a3, a4, correctAnswer);
    }

    private Question multiplicationProblem(String subject, Integer difficulty) {
        String question, a1, a2, a3, a4;
        int correctAnswer;
        int questionAnswer;
        Random rand = new Random();
        int q = rand.nextInt(1,5);
        var v1 = getRandom(difficulty);
        var v2 = getRandom(difficulty);
        var v3 = getRandom(difficulty);
        var v4 = getRandom(difficulty);

        switch(difficulty) {
            case 1 -> {
                questionAnswer = v1 * v2;
                question = v1 + " X " + v2;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 2 -> {
                questionAnswer = v1 + v2 * v3;
                question = v1 + " + " + v2 + " X " + v3;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 3 -> {
                questionAnswer = v1 * v2 - v3 * v4;
                question = v1 + " X " + v2 + " - " + v3 + " X " + v4;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            default -> throw new IllegalArgumentException("Invalid difficulty level: " + difficulty);
        }
        return new Question(null, question, subject, difficulty, a1, a2, a3, a4, correctAnswer);
    }

    private Question divisionProblem(String subject, Integer difficulty) {
        String question, a1, a2, a3, a4;
        int correctAnswer;
        int questionAnswer;
        Random rand = new Random();
        int q = rand.nextInt(1,5);
        var v1 = getRandom(difficulty);
        var v2 = getRandom(difficulty);
        var v3 = getRandom(difficulty);
        var v4 = getRandom(difficulty);

        switch(difficulty) {
            case 1 -> {
                questionAnswer = v1 / v2;
                question = v1 + " ÷ " + v2;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 2 -> {
                questionAnswer = v1 / v2 + v3;
                question = v1 + " ÷ " + v2 + " + " + v3;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            case 3 -> {
                questionAnswer = v1 * v2 / v3 + v4;
                question = v1 + " X " + v2 + " ÷ " + v3 + " + " + v4;
                List<String> answers = answerRandomizer(questionAnswer, q);
                correctAnswer = q;
                a1 = answers.get(0);
                a2 = answers.get(1);
                a3 = answers.get(2);
                a4 = answers.get(3);
            }
            default -> throw new IllegalArgumentException("Invalid difficulty level: " + difficulty);
        }
        return new Question(null, question, subject, difficulty, a1, a2, a3, a4, correctAnswer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Question question = (Question) o;
        return id != null && Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
