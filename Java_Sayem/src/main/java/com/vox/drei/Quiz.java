package com.vox.drei;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Quiz {
    private String id;
    private int quizNumber; // New field for auto-incrementing number
    private String name;
    private String category;
    private List<Question> questions;

    public Quiz() {
        this.id = UUID.randomUUID().toString();
        this.questions = new ArrayList<>();
    }

    public Quiz(String name, String category) {
        this();
        this.name = name;
        this.category = category;
    }

    // Getters and setters
    public int getQuizNumber() { return quizNumber; }
    public void setQuizNumber(int quizNumber) { this.quizNumber = quizNumber; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public void addQuestion(Question question) {
        question.setQuestionNumber(questions.size() + 1);
        questions.add(question);
    }

    public void removeQuestion(Question question) {
        int index = questions.indexOf(question);
        if (index != -1) {
            questions.remove(index);
            for (int i = index; i < questions.size(); i++) {
                questions.get(i).setQuestionNumber(i + 1);
            }
        }
    }
}