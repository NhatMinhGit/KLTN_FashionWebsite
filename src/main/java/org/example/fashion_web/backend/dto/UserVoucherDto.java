package org.example.fashion_web.backend.dto;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherDto {
    private Long userId;
    private Long voucherId;
    private String status;
}
