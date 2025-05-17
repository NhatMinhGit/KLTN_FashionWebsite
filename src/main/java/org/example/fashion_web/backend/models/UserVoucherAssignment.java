package org.example.fashion_web.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "user_voucher_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "voucher_id"})
})
public class UserVoucherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Assume you already have a User entity

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher; // Assume you already have a Voucher entity

    private LocalDateTime assignedAt = LocalDateTime.now();

    private Boolean isUsed = false;

}