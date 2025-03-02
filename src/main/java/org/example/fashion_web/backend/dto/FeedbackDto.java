package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {
    private Long feedbackId;
    private Long productId;
    private Long orderItemId;
    private Long userId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
