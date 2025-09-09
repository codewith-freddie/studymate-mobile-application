package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

public class StudentSyllabusInfo implements ListItem {
    private String id;
    private String title;
    private String url;
    private String dateTime; // Changed back to String
    private int viewCount = 0;
    private String type;
    private Long timestamp;

    // Default constructor required for Firebase
    public StudentSyllabusInfo() {
        // No-arg constructor for Firebase deserialization
    }

    // Constructor with all parameters
    public StudentSyllabusInfo(String id, String title, String url, String dateTime, int viewCount, String type, Long timestamp) {
        this.id = id;
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
