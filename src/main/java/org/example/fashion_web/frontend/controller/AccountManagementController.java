package org.example.fashion_web.frontend.controller;

import jakarta.validation.Valid;
import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.security.Principal;
import java.util.List;

@Controller

public class AccountManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    @Autowired
    private UserRepository userRepository;

//    @RequestMapping("admin/account")
//    public String accountIndex(Model model, Principal principal) {
//        List<User> users = userRepository.findAll();
//        model.addAttribute("users", users);
//        if (principal != null) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
//            if (userDetails != null) {
//                model.addAttribute("user", userDetails);
//            } else {
//                model.addAttribute("user", new User()); // Tránh lỗi NullPointerException
//            }
//        }
//        return "account/index";
//    }

    @GetMapping("admin/account")
    public String listUsersPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model, Principal principal) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.getAllUsers(pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", userPage.getTotalPages());

        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            model.addAttribute("user", userDetails);
        }

        return "account/accounts-paging";
    }

    @GetMapping("admin/account/add-account")
    public String createUser (Model model) {
        UserDto userDto = new UserDto();
        model.addAttribute("userDto", userDto);
        return "account/add-account";
    }

    @PostMapping("admin/account/add-account")
    public String createUser(@Valid @ModelAttribute("userDto") UserDto userDto, BindingResult result, Model model) {
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            result.addError(new FieldError("userDto", "email", "Email đã tồn tại!"));
        }
        if (result.hasErrors()) {
            model.addAttribute("userDto", userDto); // Đảm bảo model có dữ liệu lỗi
            return "account/add-account";
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        return "redirect:/admin/account";
    }

    @GetMapping("admin/account/edit-account")
    public String editUser(Model model, @RequestParam Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/account";
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setRole(user.getRole());
        userDto.setPassword("");

        model.addAttribute("userDto", userDto);

        return "account/edit-account"; // Đảm bảo file này tồn tại
    }


    @PostMapping("admin/account/edit-account")
    public String editUser(
            Model model,
            @RequestParam Long id,
            @Valid @ModelAttribute UserDto userDto,
            BindingResult result){
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/account";
        }

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        model.addAttribute("user", user);
        if (!userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        try {
            userRepository.save(user);
        }
        catch (Exception ex) {
            result.addError(new FieldError("userDto", "email", "Email đã tồn tại!"));
            return "account/edit-account";
        }

        return "redirect:/admin/account";
    }

    @PutMapping("admin/account/change-status/{id}")
    public ResponseEntity<String> changeStatus(@PathVariable Long id, @RequestParam boolean status) {
        boolean updated = userService.updateUserStatus(id, status);
        if (updated) {
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy tài khoản!");
        }
    }
}
