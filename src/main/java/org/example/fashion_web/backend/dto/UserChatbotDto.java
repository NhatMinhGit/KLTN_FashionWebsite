package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserChatbotDto {
    private Long chatbotId;
    private Long userId;
    private String interactionLog;
    private LocalDateTime lastInteractionAt;
}
