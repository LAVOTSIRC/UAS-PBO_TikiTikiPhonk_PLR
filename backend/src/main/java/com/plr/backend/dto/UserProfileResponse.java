package com.plr.backend.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private String username;
    private String email;
    private LocalDateTime joinedDate;
    private String profilePicturePath;

    public UserProfileResponse() {}

    public UserProfileResponse(String username, String email, LocalDateTime joinedDate, String profilePicturePath) {
        this.username = username;
        this.email = email;
        this.joinedDate = joinedDate;
        this.profilePicturePath = profilePicturePath;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDateTime joinedDate) { this.joinedDate = joinedDate; }
    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }
}
