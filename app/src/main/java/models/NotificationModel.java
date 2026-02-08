package models;

import com.google.firebase.Timestamp;

public class NotificationModel {

    private String id;

    // 🔥 MUST MATCH FIRESTORE FIELD
    private String toUserId;

    private String title;
    private String message;
    private String type;
    private Timestamp timestamp;
    private boolean read;

    // Required empty constructor
    public NotificationModel() {}

    // Getters
    public String getId() { return id; }
    public String getToUserId() { return toUserId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }
}
