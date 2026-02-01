package models;

public class comment {

    private String commentId;
    private String userId;
    private String userName;
    private String profilePicUrl;
    private String text;
    private long timestamp;

    public comment() {}

    public comment(String userId, String userName, String profilePicUrl,
                   String text, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.profilePicUrl = profilePicUrl;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getProfilePicUrl() { return profilePicUrl; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
}
