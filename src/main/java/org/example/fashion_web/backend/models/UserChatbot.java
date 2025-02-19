package org.example.fashion_web.backend.models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "User_Chatbot")
public class UserChatbot {
    @EmbeddedId
    private UserChatbotId id;

    @ManyToOne
    @MapsId("chatbotId")
    private Chatbot chatbot;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @Column(name = "interaction_log", columnDefinition = "TEXT")
    private String interactionLog;

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;
}