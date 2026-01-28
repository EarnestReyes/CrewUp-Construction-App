package models;

import com.google.firebase.Timestamp;

public class NotificationModel {

    private String id;
    private String userId;
    private String title;
    private String message;
    private String type;
    private Timestamp timestamp;
    private boolean read;

    public NotificationModel() {}

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    public void setId(String id) { this.id = id; }
}
