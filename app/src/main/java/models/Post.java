package models;

public class Post {

    private String userId;
    private String userName;
    private String title;
    private String content;
    private String timestamp;
    private String imageUrl;
    private String profilePicUrl;
    private int likeCount;
    private boolean likedByMe;
    private String postId;

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
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        this.likedByMe = likedByMe;
    }
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
