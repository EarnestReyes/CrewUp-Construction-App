package models;

import android.content.Intent;
import android.util.Log;

public class WorkerModel {

    private String username;
    private String Role;
    private double rating;
    private String profilePicUrl;

    public WorkerModel() {}

    public String getUsername() { return username; }
    public String getRole() { return Role; }
    public double getRating() { return rating; }
    public String getProfilePicUrl() { return profilePicUrl; }

}
