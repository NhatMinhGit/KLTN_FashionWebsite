package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Chatbot")
public class Chatbot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatbot_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "response_type")
    private String responseType;

    @Column(name = "conversation_log", columnDefinition = "TEXT")
    private String conversationLog;
}
