package org.example.fashion_web.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotDto {
    private String name;
    private String responseType;
    private String conversationLog;
}
