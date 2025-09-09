package com.example.studymate.UserDashboard.Room.FacultyActivity.People;

public class UserInfo {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private String role;

    // Default constructor
    public UserInfo() {}

    // Constructor with parameters
    public UserInfo(String userId, String firstName, String lastName, String email, String profileImageUrl) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
