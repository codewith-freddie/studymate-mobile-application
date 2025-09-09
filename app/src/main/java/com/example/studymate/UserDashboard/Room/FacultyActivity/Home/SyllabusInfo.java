package com.example.studymate.UserDashboard.Room.FacultyActivity.Home;

import com.example.studymate.Dialog.DateUtils;

public class SyllabusInfo implements ListItem {
    private String syllabusId;
    private String title;
    private String url;
    private String dateTime; // Changed back to String
    private int viewCount;
    private String type;
    private Long timestamp;

    // Default constructor required for Firebase
    public SyllabusInfo() {
        // No-arg constructor for Firebase deserialization
    }

    // Constructor with all parameters
    public SyllabusInfo(String syllabusId, String title, String url, String dateTime, int viewCount, String type, Long timestamp) {
        this.syllabusId = syllabusId;
        this.title = title;
        this.url = url;
        this.dateTime = dateTime;  // Now String
        this.viewCount = viewCount;
        this.type = type;
        this.timestamp = timestamp;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    // Getter and Setter for syllabusId
    public String getSyllabusId() {
        return syllabusId;
    }

    public void setSyllabusId(String syllabusId) {
        this.syllabusId = syllabusId;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for url
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Getter and Setter for dateTime
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    // Getter and Setter for countView
    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

}
