package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    public User(String role, String password, String email, String name) {
        this.role = "USER";
        this.password = password;
        this.email = email;
        this.name = name;
    }
}
