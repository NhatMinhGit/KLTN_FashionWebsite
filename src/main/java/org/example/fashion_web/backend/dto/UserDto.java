package org.example.fashion_web.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @Pattern(
            regexp = "^([A-Za-zÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯàáâãèéêìíòóôõùúăđĩũơưẠ-ỹ]+)(\\s+[A-Za-zÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯàáâãèéêìíòóôõùúăđĩũơưẠ-ỹ]+)+$   ",
            message = "Tên phải có ít nhất hai từ, mỗi từ bắt đầu bằng chữ in hoa và không chứa ký tự đặc biệt"
    )
    private String name;

    @Pattern(
            regexp = "^[a-zA-Z0-9]{6,}@gmail\\.com$",
            message = "Email phải có ít nhất 6 ký tự chữ hoặc số trước '@gmail.com' và không chứa ký tự đặc biệt khác"
    )
    private String email;

    private String role;

    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])?.{8,}$",
            message = "Gồm ít nhất một chữ cái và một số. Có thể có ký tự đặc biệt"
    )
    private String password;
}
