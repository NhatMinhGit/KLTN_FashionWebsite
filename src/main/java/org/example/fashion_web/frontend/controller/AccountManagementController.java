package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller

public class AccountManagementController {

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    @GetMapping("admin/account")
    public String accountIndex(Model model, Principal principal) {
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            } else {
                model.addAttribute("user", new User()); // Tránh lỗi NullPointerException
            }
        }
        return "account/index";
    }

    @RequestMapping("admin/add-account")
    public String add() {
        return "account/add";
    }
}
