package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "Chatbot")
public class Chatbot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatbot_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "response_type")
    private String responseType;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "intent")
    private String intent;

    @Column(name = "entities", columnDefinition = "TEXT")
    private String entities;

    @Column(name = "status")
    private String status;

    @Column(name = "conversation_log", columnDefinition = "TEXT")
    private String conversationLog;
}
