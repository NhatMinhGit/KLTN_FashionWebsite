package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Voucher findByVoucherCode(String voucherCode);

    @Query("SELECT v FROM Voucher v WHERE v.id NOT IN (SELECT uva.voucher.id FROM UserVoucherAssignment uva)")
    List<Voucher> findGeneralVouchers();

    @Query("SELECT v FROM Voucher v WHERE v.id IN (SELECT uva.voucher.id FROM UserVoucherAssignment uva)")
    List<Voucher> findAssignedVouchers();
    @Query("""
    SELECT v FROM Voucher v
    WHERE v.id NOT IN (
        SELECT uv.user.id FROM UserVoucher uv
    )
    
    AND v.id IN (
        SELECT uva.voucher.id
        FROM UserVoucherAssignment uva
        WHERE uva.user.id = :userId AND uva.isUsed = false )
    AND v.endDate >= CURRENT_DATE
""")
    List<Voucher> findAvailableVouchersForUser(Long userId);


}
