package org.example.fashion_web.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String name;
    private String email;
    private String role;
}
