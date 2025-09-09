package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.List;

public class Quiz {
    private String id;
    private String title;
    private int examineeCount;
    private int time; // Duration of the quiz in minutes
    private List<QuizQuestion> questionList;
    private String dateTime; // Add this for datetime
    private String roomId;

    // Empty constructor for Firebase
    public Quiz() {}

    public Quiz(String id, String title, int examineeCount, int time, List<QuizQuestion> questionList, String dateTime) {
        this.id = id;
        this.title = title;
        this.examineeCount = examineeCount;
        this.time = time;
        this.questionList = questionList;
        this.dateTime = dateTime; // Initialize datetime
        this.roomId = roomId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getExamineeCount() {
        return examineeCount;
    }

    public void setExamineeCount(int examineeCount) {
        this.examineeCount = examineeCount;
    }

    public int getTime() {
        return time; // Duration in minutes
    }

    public void setTime(int time) {
        this.time = time; // Set duration in minutes
    }

    public String getDateTime() {
        return dateTime; // Getter for datetime
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime; // Setter for datetime
    }

    public List<QuizQuestion> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<QuizQuestion> questionList) {
        this.questionList = questionList;
    }

    public abstract class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Default implementation
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Default implementation
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Override this method if needed
        }
    }
}
