package org.example.fashion_web.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotEmpty(message = "The name is required")
    private String name;
    @NotEmpty(message = "The email is required")
    private String email;
    @NotEmpty(message = "The role is required")
    private String role;
    @NotEmpty(message = "The password is required")
    private String password;

}

