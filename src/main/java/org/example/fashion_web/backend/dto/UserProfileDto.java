package org.example.fashion_web.backend.dto;

import lombok.*;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.Ward;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private Ward ward;
    private String avatar;
    private Date dob;
    private String phoneNumber;
    private String address;

    private User user;

}
