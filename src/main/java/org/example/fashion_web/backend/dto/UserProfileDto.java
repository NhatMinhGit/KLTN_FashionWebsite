package org.example.fashion_web.backend.dto;

import lombok.*;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.Ward;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private Ward ward;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;
    private String phoneNumber;
    private String address;
    private String avatar;

    private User user;
}
