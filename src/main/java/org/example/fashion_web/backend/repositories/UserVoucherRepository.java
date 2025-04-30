package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUser_Id(Long userId);

}