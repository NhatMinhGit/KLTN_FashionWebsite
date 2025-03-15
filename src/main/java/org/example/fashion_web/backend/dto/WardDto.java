package org.example.fashion_web.backend.dto;

import lombok.*;
import org.example.fashion_web.backend.models.Ward;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardDto {
    private String wardName;
    private Long id;

    public WardDto(Ward ward) {
        this.id = ward.getId();
        this.wardName = ward.getWardName();
    }
}
