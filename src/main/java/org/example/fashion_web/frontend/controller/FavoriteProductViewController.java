package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.FavoriteProductRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class FavoriteProductViewController {
    @Autowired
    private FavoriteProductRepository favoriteProductRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private FavoriteProductService favoriteProductService;
    @Autowired
    private UserService userService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductVariantService productVariantService;
    @Autowired
    private ImageService imageService;


    @GetMapping("/user/wishlist")
    public String showWishlist(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        Long userId = user.getId();
        model.addAttribute("userId", userId);

        // Lấy danh sách sản phẩm yêu thích của user
        List<Product> favoriteProducts = favoriteProductService.getFavoriteProductsByUserId(userId);
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
        // Đưa dữ liệu ra view
        model.addAttribute("favoriteProducts", favoriteProducts);
        return "product/favorite-products"; // Tên file Thymeleaf: wishlist.html trong thư mục templates/user/
    }
}
