package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public Voucher save(VoucherDto voucherDto) {
        Voucher voucher = new Voucher(voucherDto.getUsageLimit(), voucherDto.getEndDate(), voucherDto.getStartDate(), voucherDto.getMinOrderValue(), voucherDto.getDiscountValue(), voucherDto.getDiscountType(), voucherDto.getVoucherName(), voucherDto.getVoucherCode());
        return voucherRepository.save(voucher);
    }

    @Override
    public Page<Voucher> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

}
