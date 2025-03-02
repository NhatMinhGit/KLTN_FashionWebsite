package org.example.fashion_web.backend.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long userId;
    private Long wardId;
    private String avatar;
    private LocalDate dob;
    private String phoneNumber;
}
