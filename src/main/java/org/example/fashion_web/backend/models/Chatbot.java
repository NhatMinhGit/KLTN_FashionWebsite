package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chatbot")
public class Chatbot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatbot_id")
    private Long chatbotId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "status", columnDefinition = "VARCHAR(50) DEFAULT 'active'")
    private String status = "active";

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

}
