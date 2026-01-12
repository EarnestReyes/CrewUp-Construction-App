package com.example.ConstructionApp;

public class Post {
    private String userName;
    private String title;
    private String content;
    private String timestamp;

    public Post(String userName, String title, String content, String timestamp) {
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUserName() { return userName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
}
