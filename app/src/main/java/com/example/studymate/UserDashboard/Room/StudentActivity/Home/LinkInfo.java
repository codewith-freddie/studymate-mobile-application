package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

public class LinkInfo implements ListItem {
    private String linkId;
    private String linkTitle;
    private String link;
    private String instructions;
    private String dateTime; // Changed back to String
    private boolean isExpanded = false;  // Field for expanded state
    private Long timestamp;

    // Default constructor required for Firebase
    public LinkInfo() {}

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public String getLink() {
        return link;
    }

    // Constructor with all parameters
    public LinkInfo(String linkId,String linkTitle, String link, String instructions, String dateTime, Long timestamp) {
        this.linkId = linkId;
        this.linkTitle = linkTitle;
        this.link = link;
        this.instructions = instructions;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
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
        return 4; // Type for Announcement
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
