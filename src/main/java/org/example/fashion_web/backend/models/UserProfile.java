package org.example.fashion_web.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "UserProfiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dob;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @Column(name = "address")
    private String address;


    public UserProfile () {
        this.phoneNumber = "Chưa cập nhật!";
        this.ward = null;
        this.avatar = "/pics/default-avatar.jpg";
        this.address = "Chưa cập nhật!";
        this.user = null;
    }

    public UserProfile(User user, Ward ward, Date dob, String avatar, String phoneNumber, String address) {
        this.user = user;
        this.ward = ward;
        this.dob = dob;
        this.avatar = avatar;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
