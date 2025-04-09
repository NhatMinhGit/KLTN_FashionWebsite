package org.example.fashion_web.backend.models;

public class ChatMessage {
    private User user;
    private String content;
    private String senderType; // "admin" hoặc "user"

    public ChatMessage() {}

    public ChatMessage(User user, String content, String senderType) {
        this.user = user;
        this.content = content;
        this.senderType = senderType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }
}
