package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

public class Announcement implements ListItem {
    private String announcementId;
    private String announcement;
    private String dateTime; // Changed back to String
    private boolean isExpanded = false;  // Field for expanded state
    private Long timestamp;

    // Default constructor required for Firebase
    public Announcement() {}

    // Constructor with all parameters
    public Announcement(String announcementId, String announcement, String dateTime, Long timestamp) {
        this.announcementId = announcementId;
        this.announcement = announcement;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter and Setter for isExpanded
    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @Override
    public int getType() {
        return 2; // Type for Announcement
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String createAnnouncement) {
        this.announcement = createAnnouncement;
    }

    public String getDateTime() {
        return dateTime; // Use String for dateTime
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public long getTimestamp() {
        return timestamp != null ? timestamp : 0L; // Return the timestamp or 0 if null
    }
}
