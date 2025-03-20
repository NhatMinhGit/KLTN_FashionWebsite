package org.example.fashion_web.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Ward")
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "district_id")
    @JsonBackReference // Ngăn vòng lặp
    private District district;

    @Column(name = "ward_name", nullable = false, length = 255)
    private String wardName;

}
