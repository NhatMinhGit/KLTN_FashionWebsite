package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotDto {
    private Long id;
    private String name;
    private String responseType;
    private String conversationLog;
    private LocalDateTime createdAt;
}