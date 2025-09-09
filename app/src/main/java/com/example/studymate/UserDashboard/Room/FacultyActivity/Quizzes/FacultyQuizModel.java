package com.example.studymate.UserDashboard.Room.FacultyActivity.Quizzes;


import java.util.List;

public class FacultyQuizModel {
    private String id;
    private int examineeCount; // Use long or int based on your needs
    private String title;
    private int time;
    private String dateTime; // Add this for datetime
    private List<FacultyQuestionModel> questionList;
    private long timestamp; // Ensure this field exists

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDateTime() {
        return dateTime;
    }

    // Default constructor
    public FacultyQuizModel() {
    }

    // Parameterized constructor
    public FacultyQuizModel(String id, int examineeCount, String title, String dateTime, int time, List<FacultyQuestionModel> questionList) {
        this.id = id;
        this.examineeCount = examineeCount; // Change this to long
        this.title = title;
        this.dateTime = dateTime;
        this.time = time;
        this.questionList = questionList;
    }

    // Getter methods
    public String getId() {
        return id;
    }

    public int getExamineeCount() {
        return examineeCount; // Ensure this is long
    }

    public void setExamineeCount(int examineeCount) {
        this.examineeCount = examineeCount; // Ensure this is long
    }

    public String getTitle() {
        return title;
    }

    public int getTime() {
        return time;
    }

    public List<FacultyQuestionModel> getQuestionList() {
        return questionList;
    }
}
