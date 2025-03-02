package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "District")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "district_name", nullable = false, length = 255)
    private String districtName;
}
