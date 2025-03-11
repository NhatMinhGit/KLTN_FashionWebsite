package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserProfile;
import org.example.fashion_web.backend.services.UserProfileService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;
import java.util.Optional;

@Controller
public class ProfileController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Inject UserDetailsService

    @GetMapping("/user/profile")
    public String profilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetail) {
        if (userDetail != null) {
            User user = userDetail.getUser();
            Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileById(user.getId());

            UserProfile userProfile = userProfileOpt.orElse(new UserProfile()); // Tránh lỗi nếu user chưa có profile

            // Đưa dữ liệu vào model để sử dụng trong Thymeleaf
            model.addAttribute("currentPage", "profile");
            model.addAttribute("user", userDetail);
            model.addAttribute("userProfile", user);
            model.addAttribute("profile", userProfile);
            String address = "quận " + userProfile.getWard().getWardName() + ", phường "
                    + userProfile.getWard().getDistrict().getDistrictName()
                    + ", thành phố " + userProfile.getWard().getDistrict().getCity().getCityName();
            model.addAttribute("address", address);



        } else {
            return "redirect:/login"; // Chuyển hướng về trang đăng nhập nếu chưa đăng nhập
        }

        return "profile/profile"; // Trả về trang HTML profile
    }
}