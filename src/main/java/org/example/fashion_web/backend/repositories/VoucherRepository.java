package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Voucher findByVoucherCode(String voucherCode);
}
