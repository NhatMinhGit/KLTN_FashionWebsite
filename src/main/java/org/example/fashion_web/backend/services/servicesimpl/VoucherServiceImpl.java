package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.UserVoucher;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.UserVoucherRepository;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

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

    @Override
    public List<Voucher> getAllVouchersAvilable(Long userId) {
        // Lấy danh sách các voucher đã được người dùng sử dụng
        List<UserVoucher> userVouchers = userVoucherRepository.findByUser_Id(userId);

        // Lấy ID của các voucher đã sử dụng
        List<Long> usedVoucherIds = userVouchers.stream()
                .map(uv -> uv.getVoucher().getId())
                .toList();

        // Trả về các voucher còn hiệu lực, còn lượt sử dụng và chưa từng được dùng
        return voucherRepository.findAll().stream()
                .filter(v ->
                        !v.getEndDate().isBefore(LocalDate.now()) &&
                                v.getUsageLimit() > 0 &&
                                !usedVoucherIds.contains(v.getId())
                )
                .toList();
    }
}
