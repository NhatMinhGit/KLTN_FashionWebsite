package org.example.fashion_web.backend.configurations;

import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserProfile;
import org.example.fashion_web.backend.models.UserVoucherAssignment;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DefaultVoucherConfig {
    @Autowired
    private  UserProfileRepository userProfileRepository;
    @Autowired
    private UserVoucherRepository userVoucherRepository;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private OrderRepository orderRepository; // Order là bắt buộc trong UserVoucher
    @Autowired
    private UserVoucherAssignmentRepository userVoucherAssignmentRepository;


    // Chạy lúc 00:01 mỗi ngày
    @Scheduled(cron = "0 1 0 * * *")
    public void checkBirthdaysAndAddVouchers() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        int month = today.getMonthValue();

        List<UserProfile> birthdayUsers = userProfileRepository.findByBirthdayToday(day, month);

        if (birthdayUsers.isEmpty()) {
            return;
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherCode("BIRTHDAY" + today.getYear());
        voucher.setDiscountValue(BigDecimal.valueOf(10.0));
        voucher.setDiscountType("PERCENT");
        voucher.setUsageLimit(birthdayUsers.size());
        voucher.setStartDate(LocalDate.now());
        voucher.setEndDate(LocalDate.now().plusDays(7));

        voucher = voucherRepository.save(voucher); // lưu vào DB

        for (UserProfile profile : birthdayUsers) {
            User user = profile.getUser();

            UserVoucherAssignment userVoucherAssignment = new UserVoucherAssignment();
            userVoucherAssignment.setUser(user);
            userVoucherAssignment.setVoucher(voucher);
            userVoucherAssignment.setAssignedAt(LocalDate.now().atStartOfDay());
            userVoucherAssignment.setIsUsed(false);

            userVoucherAssignmentRepository.save(userVoucherAssignment);
        }
    }
}
