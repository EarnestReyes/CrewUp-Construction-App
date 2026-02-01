package models;

import java.io.Serializable;

public class UserModel implements Serializable {

    private String userId;
    private String username;
    private String email;
    private String location;
    private String fcmToken;
    private String Role;

    public UserModel() {}

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getLocation() { return location; }
    public String getFcmToken() { return fcmToken; }
    public String getRole(){
        return Role;
    }

    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setLocation(String location) { this.location = location; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
}
