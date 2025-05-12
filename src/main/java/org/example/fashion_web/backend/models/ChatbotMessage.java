package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "chatbot_message")
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "session_id")
    private ChatbotSession chatbotSession;

    @Column(name = "sender_type", columnDefinition = "VARCHAR(50)")
    private String senderType;

    @Column(name = "message_text", columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "message_type", columnDefinition = "VARCHAR(50)")
    private String messageType;

    @Column(name = "intent", columnDefinition = "VARCHAR(255)")
    private String intent;

    @Column(name = "entities", columnDefinition = "TEXT")
    private String entities;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}
