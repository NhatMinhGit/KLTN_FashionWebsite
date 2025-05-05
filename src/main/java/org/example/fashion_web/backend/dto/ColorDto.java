package org.example.fashion_web.backend.dto;

public class ColorDto {
    private String label; // Đen, Đỏ
    private String value; // black, red

    public ColorDto(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public String getValue() { return value; }
}
