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

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatbotId")
    @JoinColumn(name = "chatbot_id", nullable = false)
    private Chatbot chatbot;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "interaction_log", columnDefinition = "TEXT")
    private String interactionLog;

    @Column(name = "last_interaction_at", nullable = false)
    private LocalDateTime lastInteractionAt;
}
