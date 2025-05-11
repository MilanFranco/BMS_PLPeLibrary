package com.example.bms_plpelibrary.models;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role;  // "STUDENT", "PROFESSOR", "ADMIN"
    private String course;
    private String department;
    private String profileImage;

    // Constructors, getters, setters
    public User() {
        // Required empty constructor for Firebase
    }

    public User(String userId, String email, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}