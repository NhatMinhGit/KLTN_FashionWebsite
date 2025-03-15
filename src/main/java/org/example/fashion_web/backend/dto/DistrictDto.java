package org.example.fashion_web.backend.dto;

import lombok.Data;
import org.example.fashion_web.backend.models.District;

@Data
public class DistrictDto {
    private Long id;
    private String districtName;

    public DistrictDto(District district) {
        this.id = district.getId();
        this.districtName = district.getDistrictName();
    }

}
