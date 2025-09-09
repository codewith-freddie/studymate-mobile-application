package com.example.studymate.UserDashboard.Room.StudentActivity.Leaderboard;

public class UserScore {
    private String firstName;
    private String lastName;
    private Long score;
    private String profileImageUrl;


    // Default constructor required for calls to DataSnapshot.getValue(UserScore.class)
    public UserScore() { }

    // Constructor with all fields
    public UserScore(String firstName, String lastName, Long score, String profileImageUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.score = score;
        this.profileImageUrl = profileImageUrl;

    }

    // Getter for first name
    public String getFirstName() {
        return firstName;
    }

    // Getter for last name
    public String getLastName() {
        return lastName;
    }

    // Getter for score
    public Long getScore() {
        return score;
    }

    // Getter for profile image URL
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
