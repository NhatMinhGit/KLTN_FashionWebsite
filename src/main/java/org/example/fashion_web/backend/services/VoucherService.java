package org.example.fashion_web.backend.services;


import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    Voucher save(VoucherDto voucherDto);
    Page<Voucher> getAllVouchers(Pageable pageable);
    List<Voucher> getAllVouchers();

}
