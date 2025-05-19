package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer branchId;

    @NotNull
    @Column(nullable = false)
    private String branchName;

    @NotNull
    @Column(nullable = false, length = 500)
    private String address;

    private String city;
    private String district;
    private String ward;
    private String phone;
    private Double latitude;
    private Double longitude;
}
