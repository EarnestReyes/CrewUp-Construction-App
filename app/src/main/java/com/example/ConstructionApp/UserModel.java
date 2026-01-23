package com.example.ConstructionApp;

public class UserModel {

    private String username;
    private String email;
    private String location;

    private Double lat;
    private Double lng;

    private Long createdAt;
    private Long locationUpdatedAt;

    // REQUIRED empty constructor for Firestore
    public UserModel() {}

    // Getters
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getLocation() {
        return location;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getLocationUpdatedAt() {
        return locationUpdatedAt;
    }
}
