package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.UserVoucher;
import org.example.fashion_web.backend.models.UserVoucherAssignment;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.UserVoucherAssignmentRepository;
import org.example.fashion_web.backend.repositories.UserVoucherRepository;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;
    @Autowired
    private UserVoucherAssignmentRepository userVoucherAssignmentRepository;

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

    public List<Voucher> getAllVouchersAvilable(Long userId) {
        List<Voucher> allVouchers = voucherRepository.findAll();

        List<UserVoucherAssignment> userAssignments = userVoucherAssignmentRepository
                .findByUserIdAndIsUsed(userId, false);

        Set<Long> usedVoucherIds = userVoucherRepository
                .findAll()
                .stream()
                .map(uv -> uv.getVoucher().getId())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();

        return allVouchers.stream()
                .filter(v ->
                        // 1. Là voucher trong danh sách assignment của user
                        userAssignments.stream()
                                .anyMatch(uva -> uva.getVoucher().getId().equals(v.getId()))
                                // 2. Không nằm trong danh sách voucher đã dùng
                                && !usedVoucherIds.contains(v.getId())
                                // 3. Còn hạn sử dụng
                                && !v.getEndDate().isBefore(today)
                )
                .collect(Collectors.toList());
    }


    @Override
    public List<Voucher> searchVouchersByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return voucherRepository.findAll();

        String lowerKeyword = keyword.toLowerCase();
        return voucherRepository.findAll().stream()
                .filter(u -> String.valueOf(u.getId()).equals(keyword) ||
                        u.getVoucherCode().toLowerCase().contains(lowerKeyword) ||
                        u.getVoucherName().toLowerCase().contains(lowerKeyword) ||
                        u.getDiscountType().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
    @Override

    public List<Voucher> getGeneralVouchers() {
        return voucherRepository.findGeneralVouchers();

    public List<Voucher> getGeneralVouchers(Long userId) {
        // Tất cả voucher trong hệ thống
        List<Voucher> allVouchers = voucherRepository.findAll();

        // Danh sách voucher đã được assign cho bất kỳ ai
        List<Voucher> voucherAssignments = voucherRepository.findAssignedVouchers();
        List<Long> voucherAssignmentIds = voucherAssignments.stream()
                .map(Voucher::getId)
                .toList();

        // Danh sách voucher người dùng đã dùng
        List<UserVoucher> userVouchers = userVoucherRepository.findByUser_Id(userId);
        List<Long> usedVoucherIds = userVouchers.stream()
                .map(uv -> uv.getVoucher().getId())
                .toList();

        // Lọc ra các voucher còn hạn, còn lượt, không bị assign và chưa từng dùng
        return allVouchers.stream()
                .filter(v ->
                        !v.getEndDate().isBefore(LocalDate.now()) &&
                                v.getUsageLimit() > 0 &&
                                !usedVoucherIds.contains(v.getId()) &&
                                !voucherAssignmentIds.contains(v.getId())
                )
                .toList();
    }


    @Override
    public List<Voucher> getVoucherAvilable() {
        // Trả về các voucher còn hiệu lực, còn lượt sử dụng và chưa từng được dùng
        return voucherRepository.findAll().stream()
                .filter(v ->
                        !v.getEndDate().isBefore(LocalDate.now()) &&
                                v.getUsageLimit() > 0
                )
                .toList();
    }

    @Override
    public List<Voucher> getVoucherNotAssign() {
        List<Voucher> list = voucherRepository.findGeneralVouchers();

        return list.stream()
                .filter(v ->
                        !v.getEndDate().isBefore(LocalDate.now()) &&
                                v.getUsageLimit() > 0
                )
                .toList();
    }

}
