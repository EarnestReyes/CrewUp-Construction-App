package models;

import com.google.firebase.Timestamp;

public class NotificationModel {

    private String id;
    private String toUserId;
    private String title;
    private String message;
    private String type;
    private Timestamp timestamp;
    private boolean read;

    // REQUIRED empty constructor
    public NotificationModel() {}

    // Getters
    public String getId() { return id; }
    public String getToUserId() { return toUserId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    // 🔥 ADD ALL SETTERS
    public void setId(String id) { this.id = id; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setRead(boolean read) { this.read = read; }
}
