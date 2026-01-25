package com.example.ConstructionApp;

public class Post {

    private String userId;
    private String userName;
    private String title;
    private String content;
    private String timestamp;
    private String profilePicUrl;

    // Required empty constructor (Firestore)
    public Post() {}

    // Used in Posts (My Posts)
    public Post(String userId,
                String userName,
                String title,
                String content,
                String timestamp) {

        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Used in Home (All users)
    public Post(String userId,
                String userName,
                String title,
                String content,
                String timestamp,
                String profilePicUrl) {

        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.profilePicUrl = profilePicUrl;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public String getProfilePicUrl() { return profilePicUrl; }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
