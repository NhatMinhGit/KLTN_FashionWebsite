package org.example.fashion_web.backend.models;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chatbot_session")
public class ChatbotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "chatbot_id", referencedColumnName = "chatbot_id")
    private Chatbot chatbot;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "started_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime startedAt;

}
