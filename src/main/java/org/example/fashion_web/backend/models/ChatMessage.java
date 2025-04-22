package org.example.fashion_web.backend.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatMessage {
    // Getters và Setters
    private String sender;
    private String recipient;
    private String content;
    private String roomId;

}
