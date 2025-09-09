package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestion {

    private String question;
    private List<String> options; // List to hold options
    private String correct; // Store correct answer as a string

    public QuizQuestion() {
        // Initialize fields to avoid null values
        question = "";
        options = new ArrayList<>();
        correct = ""; // Indicates no option selected
    }

    // Getters and setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrect() { return correct; }
    public void setCorrect(String correct) { this.correct = correct; }

    // Method to check if any required field is missing
    public boolean isInvalid() {
        return question.isEmpty() || options.isEmpty() || correct.isEmpty();
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public void updateQuestion(String question, List<String> options) {
        setQuestion(question);
        setOptions(options);
    }
}
