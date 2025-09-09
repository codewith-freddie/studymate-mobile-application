package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

public class PdfInfo implements ListItem {
    private String moduleId;
    private String title;
    private String url;
    private String dateTime;
    private int downloadCount;
    private int viewCount;
    private String type;
    private long timestamp;

    // Default constructor required for Firebase
    public PdfInfo() {}

    // Constructor with all parameters
    public PdfInfo(String moduleId, String title, String url, String dateTime, int viewCount, int downloadCount, String type, long timestamp) {
        this.moduleId = moduleId;
        this.title = title;
        this.url = url;
        this.dateTime = dateTime;
        this.viewCount = viewCount;
        this.downloadCount = downloadCount;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getType() {
        return 1; // Type for PdfInfo
    }
}
