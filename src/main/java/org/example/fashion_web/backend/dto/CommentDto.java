package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long commentId;
    private Long feedbackId;
    private Long userId;
    private String comment;
    private LocalDateTime createdAt;
}
