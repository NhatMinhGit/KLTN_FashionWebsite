package org.example.fashion_web.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class UserVoucherAssignmentDto {
    @NotNull(message = "Vui lòng chọn ít nhất một người dùng.")
    private List<Long> userIds;

    @NotNull(message = "Vui lòng chọn voucher.")
    private Long voucherId;
}

