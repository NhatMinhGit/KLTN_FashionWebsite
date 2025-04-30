package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.CartItemService;
import org.example.fashion_web.backend.services.CartService;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ImageService imageService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session, @AuthenticationPrincipal CustomUserDetails userDetail) {
        if (userDetail != null) {
            User user = userRepository.findById(userDetail.getUser().getId()).orElseThrow();
            model.addAttribute("user", user);
            List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");
            if (cart == null) {
                cart = new ArrayList<>();
            }
            // Nhóm danh sách ảnh theo productId
            Map<Long, List<String>> productImages = new HashMap<>();
            for (CartItems item : cart) {
                List<Image> images = imageService.findImagesByProductVariantId(item.getProduct().getId());
                List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
                productImages.put(item.getProduct().getId(), imageUrls);
            }
//            model.addAttribute("userList", userService.findAll());
            model.addAttribute("productImages", productImages);
            model.addAttribute("countCart", cart.size());
            model.addAttribute("totalOrderPrice", cartItemService.getTotalPrice(cart));
            model.addAttribute("cartItems", cart);
//            //Lấy số lượng cart của user
//
//            Integer countCart = cartService.getCountCart(user.getId());
//            if (countCart == null) {
//                countCart = 0;
//            }
//            model.addAttribute("countCart", countCart);
        } else {
            model.addAttribute("countCart", 0);
        }
    }
}