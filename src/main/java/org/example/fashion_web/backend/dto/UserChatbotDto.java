package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChatbotDto {
    private String interactionLog;
    private LocalDateTime lastInteractionAt;
}
