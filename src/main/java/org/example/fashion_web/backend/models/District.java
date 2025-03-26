package org.example.fashion_web.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    @JoinColumn(name = "city_id")
    @JsonBackReference
    private City city;

    @OneToMany(mappedBy = "district")
    @JsonManagedReference // Giữ quan hệ một chiều
    private List<Ward> wards;

    @Column(name = "district_name", nullable = false, length = 255)
    private String districtName;

}
