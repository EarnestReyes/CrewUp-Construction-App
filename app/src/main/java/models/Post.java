package models;

public class Post {

    private String postId;
    private String userId;
    private String userName;
    private String title;
    private String content;
    private long timestamp;
    private String imageUrl;
    private String profilePicUrl;
    private int likeCount;
    private boolean likedByMe;

    // Firestore required
    public Post() {}

    // Full constructor
    public Post(String userId,
                String userName,
                String title,
                String content,
                long timestamp,
                String profilePicUrl,
                String imageUrl) {

        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.profilePicUrl = profilePicUrl;
        this.imageUrl = imageUrl;
        this.likeCount = 0;
        this.likedByMe = false;
    }

    // No image constructor
    public Post(String userId,
                String userName,
                String title,
                String content,
                long timestamp,
                String profilePicUrl) {

        this(userId, userName, title, content, timestamp, profilePicUrl, null);
    }

    // Getters & setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    public long getTimestamp() { return timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
}
