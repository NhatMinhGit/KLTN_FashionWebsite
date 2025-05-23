package org.example.fashion_web.backend.services;


import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {
    Voucher save(VoucherDto voucherDto);
    Page<Voucher> getAllVouchers(Pageable pageable);
    List<Voucher> getAllVouchers();
    List<Voucher> getAllVouchersAvilable(Long userId);

    List<Voucher> searchVouchersByKeyword(String keyword);

    List<Voucher> getGeneralVouchers(Long userId);

    List<Voucher> getVoucherAvilable();

    List<Voucher> getVoucherNotAssign();

}
