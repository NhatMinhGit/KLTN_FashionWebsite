
package org.example.fashion_web.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.repositories.ProductVariantRepository;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class UserController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DiscountService discountService;


    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("register_user") UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                return "redirect:/admin";
            } else {
                return "redirect:/user";
            }
        }
        return "register";
    }
    @PostMapping("/registration")
    public String saveUser(@ModelAttribute("register_user") UserDto userDto, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user == null) {
            userService.save(userDto);
            redirectAttributes.addFlashAttribute("message", "Registered Successfully!");
            return "redirect:/login"; // Chuyển hướng đến trang login và hiển thị thông báo
        } else {
            redirectAttributes.addFlashAttribute("message", "Email đã được sử dụng!");
            return "redirect:/registration"; // Chuyển hướng về lại trang đăng ký kèm thông báo
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            // Người dùng đã đăng nhập -> chuyển hướng sang trang phù hợp
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
                return "redirect:/admin";
            } else {
                return "redirect:/user";
            }
        }
        return "login";
    }

    @GetMapping("/users")
    @ResponseBody
    public List<String> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
    @GetMapping({"/", "/user"})
    public String userPage(Model model, Principal principal) throws JsonProcessingException {
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            model.addAttribute("user", userDetails);

            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", user);
        }

        // Lấy danh sách sản phẩm
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Lấy giảm giá cho sản phẩm
        for (Product product : products) {
            List<ProductDiscount> productDiscounts = discountService.getActiveDiscountsForProduct(product);

            // Lấy giảm giá cho danh mục
            List<ProductDiscount> categoryDiscounts = discountService.getActiveDiscountsForCategory(product.getCategory());

            // Gộp cả 2 danh sách giảm giá
            Stream<ProductDiscount> allDiscounts = Stream.concat(
                    productDiscounts.stream(),
                    categoryDiscounts.stream()
            );

            // Tìm giảm giá cao nhất
            Optional<ProductDiscount> maxDiscount = allDiscounts
                    .max(Comparator.comparing(ProductDiscount::getDiscountPercent));

            // Áp dụng giảm giá cao nhất (nếu có)
            BigDecimal effectivePrice = maxDiscount
                    .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());

            // Cập nhật giá hiệu lực cho sản phẩm
            product.setEffectivePrice(effectivePrice);
        }

        List<User> userList = userRepository.findAll();
        model.addAttribute("userList", userList);
        Map<Long, Integer> discountPercents = new HashMap<>();
        for (Product product : products) {
            if (product.getEffectivePrice() != null && product.getEffectivePrice().compareTo(product.getPrice()) < 0) {
                BigDecimal discount = product.getPrice().subtract(product.getEffectivePrice());
                BigDecimal percent = discount.divide(product.getPrice(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                discountPercents.put(product.getId(), percent.intValue());
            } else {
                discountPercents.put(product.getId(), 0);
            }
        }
        model.addAttribute("discountPercents", discountPercents);

        // Lấy danh sách ảnh chung cho từng sản phẩm
        Map<Long, Map<Long, List<String>>> productVariantImages = new HashMap<>();

        for (Product product : products) {
            List<ProductVariant> variants = productVariantService.findAllByProductId(product.getId());
            Map<Long, List<String>> variantImageMap = new HashMap<>();

            for (ProductVariant variant : variants) {
                List<Image> images = imageService.findImagesByProductVariantId(variant.getId());
                List<String> imageUrls = images.stream()
                        .map(Image::getImageUri)
                        .collect(Collectors.toList());
                variantImageMap.put(variant.getId(), imageUrls);
            }

            productVariantImages.put(product.getId(), variantImageMap);
        }
        // Gửi danh sách ảnh theo productId vào model
        model.addAttribute("productVariantImages", productVariantImages);

        List<Product> deepDiscountProducts = products.stream()
                .filter(p -> discountPercents.get(p.getId()) >= 10) // Giảm từ 30% trở lên
                .sorted(Comparator.comparing((Product p) -> discountPercents.get(p.getId())).reversed())
                .collect(Collectors.toList());
        model.addAttribute("deepDiscountProducts", deepDiscountProducts);


//        // Trong phương thức @GetMapping("/user")
//        List<Product> preSaleProducts = products.stream()
//                .filter(p -> p.getEffectivePrice().compareTo(p.getPrice()) < 0)
//                .sorted(Comparator.comparing(Product::getEffectivePrice))
//                .limit(10)
//                .collect(Collectors.toList());
//        for (Product preSaleProduct : preSaleProducts) {
//            List<ProductVariant> variants = productVariantService.findAllByProductId(preSaleProduct.getId());
//            preSaleProduct.setVariants(variants);
//        }
//        model.addAttribute("preSaleProducts", preSaleProducts);
//        Map<Long, Integer> discountPreSalePercents = new HashMap<>();
//        for (Product product : preSaleProducts) {
//            if (product.getEffectivePrice() != null && product.getEffectivePrice().compareTo(product.getPrice()) < 0) {
//                BigDecimal discount = product.getPrice().subtract(product.getEffectivePrice());
//                BigDecimal percent = discount.divide(product.getPrice(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
//                discountPreSalePercents.put(product.getId(), percent.intValue());
//            } else {
//                discountPreSalePercents.put(product.getId(), 0);
//            }
//        }
//        model.addAttribute("discountPreSalePercents", discountPreSalePercents);
//
//        // Lấy danh sách ảnh chung cho từng sản phẩm
//        Map<Long, Map<Long, List<String>>> productVariantPreSaleImages = new HashMap<>();
//
//        for (Product preSaleProduct : preSaleProducts) {
//            List<ProductVariant> variants = productVariantService.findAllByProductId(preSaleProduct.getId());
//            Map<Long, List<String>> variantPreSaleImageMap = new HashMap<>();
//
//            for (ProductVariant preSaleVariant : variants) {
//                List<Image> images = imageService.findImagesByProductVariantId(preSaleVariant.getId());
//                List<String> imageUrls = images.stream()
//                        .map(Image::getImageUri)
//                        .collect(Collectors.toList());
//                variantPreSaleImageMap.put(preSaleVariant.getId(), imageUrls);
//            }
//
//            productVariantPreSaleImages.put(preSaleProduct.getId(), variantPreSaleImageMap);
//        }
//        // Gửi danh sách ảnh theo productId vào model
//        model.addAttribute("productVariantPreSaleImages", productVariantPreSaleImages);
        return "user";
    }

    @GetMapping("admin")
    public String adminPage (Model model, Principal principal) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("totalOrder", orderService.getTotalOrdersNotCancelled());
        model.addAttribute("totalUser", userService.getUserCount("USER"));
        model.addAttribute("revenuetotal", orderService.getTotalRevenueCompletedOrders());
        model.addAttribute("user", userDetails);
        model.addAttribute("userList", userService.findAll());
        return "admin";
    }
    @GetMapping("/about")
    public String aboutPage() {
        return "about-us"; // Trả về tên file template "about-us.html"
    }
}
