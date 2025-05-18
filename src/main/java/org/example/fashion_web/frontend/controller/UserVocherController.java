package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserVoucher;
import org.example.fashion_web.backend.models.UserVoucherAssignment;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.UserVoucherAssignmentRepository;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.VoucherService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserVocherController {
    @Autowired
    private VoucherService voucherService;

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserVoucherAssignmentRepository userVoucherAssignmentRepository;

    @RequestMapping("user/voucher")
    public String voucherIndex(Model model, Principal principal,@AuthenticationPrincipal CustomUserDetails userDetail) {
        Long userid = userDetail.getUser().getId();
        List<Voucher> generalVouchers = voucherService.getGeneralVouchers(userid);

        // Lấy danh sách voucher riêng được gán cho người dùng
        List<UserVoucherAssignment> userVoucherAssignments = userVoucherAssignmentRepository.findByUserIdAndIsUsed(userid, false);

        // Lấy danh sách các userVoucherAssignmentId
        List<Long> userVoucherAssignmentIds = userVoucherAssignments.stream()
                .map(UserVoucherAssignment::getAssignmentId)
                .collect(Collectors.toList());

        // Trích voucher từ UserVoucher
        List<Voucher> privateVouchers = userVoucherAssignments.stream()
                .map(UserVoucherAssignment::getVoucher)
                .collect(Collectors.toList());

        // Gộp cả hai loại voucher
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.addAll(generalVouchers);
        vouchers.addAll(privateVouchers);
        model.addAttribute("currentPage", "voucher");
        model.addAttribute("vouchers", generalVouchers);

        return "user-voucher/user-voucher";
    }
}
