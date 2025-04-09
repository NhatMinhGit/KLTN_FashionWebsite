package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserVoucher;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class UserVocherController {
    @Autowired
    private VoucherService voucherService;

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    @Autowired
    private VoucherRepository voucherRepository;

    @RequestMapping("user/voucher")
    public String voucherIndex(Model model, Principal principal) {

        List<Voucher> vouchers = voucherRepository.findAll();
        model.addAttribute("currentPage", "voucher");
        model.addAttribute("vouchers", vouchers);
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            } else {
                model.addAttribute("user", new User()); // Tránh lỗi NullPointerException
            }
        }
        return "user-voucher/user-voucher";
    }
}
