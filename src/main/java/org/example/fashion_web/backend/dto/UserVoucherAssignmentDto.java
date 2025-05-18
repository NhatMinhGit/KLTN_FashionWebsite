package org.example.fashion_web.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVoucherAssignmentDto {
    @NotNull(message = "Vui lòng chọn người dùng")
    private Long userId;

    @NotNull(message = "Vui lòng chọn voucher")
    private Long voucherId;

}
