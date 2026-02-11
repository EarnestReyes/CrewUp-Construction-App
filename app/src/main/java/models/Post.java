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

    // =========================
    // SHARE FIELDS
    // =========================
    private boolean isShared;
    private String originalPostId;

    private String originalUserId;
    private String originalUserName;
    private String originalProfilePicUrl;
    private String originalContent;
    private String originalImageUrl;

    // =========================
    // EMPTY CONSTRUCTOR (Firestore required)
    // =========================
    public Post() {
        this.likeCount = 0;
        this.likedByMe = false;
        this.isShared = false;
    }

    // =========================
    // NORMAL POST CONSTRUCTOR
    // =========================
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
        this.isShared = false;
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

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

    // =========================
    // SHARE GETTERS & SETTERS
    // =========================

    public boolean isShared() { return isShared; }
    public void setShared(boolean shared) { isShared = shared; }

    public String getOriginalPostId() { return originalPostId; }
    public void setOriginalPostId(String originalPostId) {
        this.originalPostId = originalPostId;
    }

    public String getOriginalUserId() { return originalUserId; }
    public void setOriginalUserId(String originalUserId) {
        this.originalUserId = originalUserId;
    }

    public String getOriginalUserName() { return originalUserName; }
    public void setOriginalUserName(String originalUserName) {
        this.originalUserName = originalUserName;
    }

    public String getOriginalProfilePicUrl() { return originalProfilePicUrl; }
    public void setOriginalProfilePicUrl(String originalProfilePicUrl) {
        this.originalProfilePicUrl = originalProfilePicUrl;
    }

    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getOriginalImageUrl() { return originalImageUrl; }
    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }
}
