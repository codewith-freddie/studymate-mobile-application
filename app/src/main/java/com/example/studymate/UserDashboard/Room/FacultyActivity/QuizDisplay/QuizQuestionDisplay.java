package com.example.studymate.UserDashboard.Room.FacultyActivity.QuizDisplay;

import java.util.List;

public class QuizQuestionDisplay {
    private String questionText;
    private List<String> optionList;
    private String correctAnswer;

    public QuizQuestionDisplay(String questionText, List<String> optionList, String correctAnswer) {
        this.questionText = questionText;
        this.optionList = optionList;
        this.correctAnswer = correctAnswer; // Assign correctAnswer here
    }

    public String getQuestionText() { return questionText; }
    public List<String> getOptionList() { return optionList; }
    public String getCorrectAnswer() { return correctAnswer; }
}
