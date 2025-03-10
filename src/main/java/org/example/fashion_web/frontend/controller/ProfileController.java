package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserProfile;
import org.example.fashion_web.backend.services.UserProfileService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetail;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Inject UserDetailsService

    @GetMapping("user/profile")
    public String profilePage(Model model, @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (customUserDetail != null) {
            User user = userService.findByEmail(customUserDetail.getUsername());
            model.addAttribute("user", user);
            model.addAttribute("profile", user.getProfile());
        } else {
            model.addAttribute("user", new User()); // Tránh lỗi NullPointerException
        }


        return "profile/profile";
    }
}