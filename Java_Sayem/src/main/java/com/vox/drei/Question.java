package com.vox.drei;

import java.util.List;
import java.util.UUID;

public class Question {
    private String id;
    private int questionNumber;
    private String question;
    private List<String> answers;
    private String correctAnswer;
    private String type;
    private String userAnswer;

    public Question() {
        this.id = UUID.randomUUID().toString();
    }

    public Question(String question, List<String> answers, String correctAnswer, String type) {
        this();
        this.question = question;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.type = type;
        this.userAnswer = "";
    }

    // Getters and setters
    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getAnswers() { return answers; }
    public void setAnswers(List<String> answers) { this.answers = answers; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public boolean isCorrectAnswer(String answer) {
        return answer.equals(correctAnswer);
    }
}