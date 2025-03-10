package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "UserProfiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dob;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;
}
