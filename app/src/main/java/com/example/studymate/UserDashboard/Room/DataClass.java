package com.example.studymate.UserDashboard.Room;

import java.util.Map;

public class DataClass {

    private String dataTitle;
    private String dataDesc;
    private String key;
    private String createdBy;
    private String accessCode;
    private int personCount; // Added field
    private Map<String, Boolean> participants;
    private String roomBackgroundImage;

    // Constructor with personCount
    public DataClass(String dataTitle, String dataDesc, String createdBy, String accessCode, int personCount, String roomBackgroundImage) {
        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.createdBy = createdBy;
        this.accessCode = accessCode;
        this.personCount = personCount;
        this.roomBackgroundImage = roomBackgroundImage;
    }

    // Default constructor
    public DataClass() {
    }
    // Getter and setter for roomBackgroundImage
    public String getRoomBackgroundImage() {
        return roomBackgroundImage;
    }

    public void setRoomBackgroundImage(String roomBackgroundImage) {
        this.roomBackgroundImage = roomBackgroundImage;
    }

    // Getters and setters
    public String getDataTitle() {
        return dataTitle;
    }

    public void setDataTitle(String dataTitle) {
        this.dataTitle = dataTitle;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public int getPersonCount() {
        return personCount;
    }

    public void setPersonCount(int personCount) {
        this.personCount = personCount;
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Boolean> participants) {
        this.participants = participants;
    }
}
