package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.UserVoucherAssignment;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVoucherAssignmentRepository extends JpaRepository<UserVoucherAssignment, Long> {
    Optional<UserVoucherAssignment> findByUserAndVoucher(User user, Voucher voucher);

    boolean existsByUserAndVoucher(User user, Voucher voucher);

//    Optional<UserVoucherAssignment> findByUserIdAndVoucherCodeAndIsUsedFalse(Long userId, String voucherCode);

    List<UserVoucherAssignment> getAssignmentsByVoucherId(Long id);

    List<UserVoucherAssignment> getAssignmentsByUserId(Long id);

}