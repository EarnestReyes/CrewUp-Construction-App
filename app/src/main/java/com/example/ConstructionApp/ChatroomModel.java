package com.example.ConstructionApp;

import com.google.firebase.Timestamp;
import java.util.List;

public class ChatroomModel {

    private String chatroomId;
    private List<String> userIds;
    private Timestamp lastMessageTimestamp;
    private String lastMessageSenderId;
    private String lastMessage;

    public ChatroomModel() {
    }

    public ChatroomModel(
            String chatroomId,
            List<String> userIds,
            String lastMessage,
            String lastMessageSenderId,
            Timestamp lastMessageTimestamp
    ) {
        this.chatroomId = chatroomId;
        this.userIds = userIds;
        this.lastMessage = lastMessage;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // ---------- Getters & Setters ----------

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
