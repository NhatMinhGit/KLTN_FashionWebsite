package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.ProductService;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ProductService productService;
    @GetMapping("/registration")
    public String getRegistrationPage(@ModelAttribute("user") UserDto userDto) {
        return "register";
    }

    @PostMapping("/registration")
    public String saveUser(@ModelAttribute("user") UserDto userDto, Model model) {
        userService.save(userDto);
        model.addAttribute("message", "Registered Successfuly!");
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("user")
    public String userPage(Model model, Principal principal) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);

        // Lấy danh sách sản phẩm
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Nhóm danh sách ảnh theo productId
        Map<Long, List<String>> productImages = new HashMap<>();
        for (Product product : products) {
            List<Image> images = imageService.findImagesByProductId(product.getId());
            List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
            productImages.put(product.getId(), imageUrls);
        }

        // Gửi danh sách ảnh theo productId vào model
        model.addAttribute("productImages", productImages);

        return "user";
    }

    //    @GetMapping("/user-page")
//    public String listBestSalerProducts(Model model, Principal principal) {
//        // Xử lý thông tin người dùng nếu có
//        if (principal != null) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
//            if (userDetails != null) {
//                model.addAttribute("user", userDetails);
//            }
//        }
//
//        // Lấy danh sách sản phẩm
//        List<Product> products = productService.getAllProducts();
//        model.addAttribute("products", products);
//        //System.out.println("ProductManagementController 96 ==> products: "+products);
//        return "user"; // user.html
//    }
    @GetMapping("admin")
    public String adminPage (Model model, Principal principal) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);
        return "admin";
    }


}