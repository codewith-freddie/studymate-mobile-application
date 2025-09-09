package com.example.studymate.AdminDashboard.AdminHome.People;

public class AdminUser {
    private String id;
    private String firstName;
    private String lastName;
    private String imageUrl;

    public AdminUser() {
        // Default constructor required for calls to DataSnapshot.getValue(AdminUser.class)
    }

    public AdminUser(String id, String firstName, String lastName, String imageUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
