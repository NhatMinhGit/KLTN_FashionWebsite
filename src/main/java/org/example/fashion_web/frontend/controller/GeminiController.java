package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.services.*;
import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private SizeService sizeService;


    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message, Principal principal) {
        String response;
        String userName = principal.getName();
        User user = userService.findByEmail(userName);
        Chatbot chatbot = chatbotService.findChatBotByUserId(user.getId());
        System.out.println("Chatbot ID: " + (chatbot != null ? chatbot.getId() : "null"));
        System.out.println("Id người dùng save vào chatlog " + user.getId());

        // Kiểm tra nếu người dùng hỏi về số lượng hàng tồn
        if (isStockQuery(message)) {
            response = geminiService.checkStock(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isPriceAndCategory(message)) {
            response = geminiService.checkPriceAndCategory(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isPrice(message)) {
            response = geminiService.checkPriceAndCategory(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else {
            response = geminiService.chatWithAI(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }

        return ResponseEntity.ok(response);
    }

    private boolean isStockQuery(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("còn không") ||
                lowerCaseMessage.contains("còn hàng không") ||
                lowerCaseMessage.contains("có hàng không") ||
                lowerCaseMessage.contains("số lượng");

    }

    private boolean isPrice(String message) {
        String lowerCaseMessage = message.toLowerCase();

        // Danh sách từ khóa liên quan đến giá
        return lowerCaseMessage.contains("giá") ||
                lowerCaseMessage.contains("bao nhiêu tiền") ||
                lowerCaseMessage.contains("khoảng") ||
                lowerCaseMessage.contains("tầm") ||
                lowerCaseMessage.contains("từ") ||
                lowerCaseMessage.contains("tới") ||
                lowerCaseMessage.contains("dưới") ||
                lowerCaseMessage.matches(".*\\d+.*k.*") || // ví dụ: 500k
                lowerCaseMessage.matches(".*\\d+.*nghìn.*"); // ví dụ: 200 nghìn
    }

    private boolean isPriceAndCategory(String message) {
        String lowerCaseMessage = message.toLowerCase();

        // Danh sách từ khóa liên quan đến giá
        boolean containsPriceKeywords =
                lowerCaseMessage.contains("giá") ||
                        lowerCaseMessage.contains("bao nhiêu tiền") ||
                        lowerCaseMessage.contains("khoảng") ||
                        lowerCaseMessage.contains("tầm") ||
                        lowerCaseMessage.contains("từ") ||
                        lowerCaseMessage.contains("tới") ||
                        lowerCaseMessage.contains("dưới") ||
                        lowerCaseMessage.matches(".*\\d+.*k.*") || // ví dụ: 500k
                        lowerCaseMessage.matches(".*\\d+.*nghìn.*"); // ví dụ: 200 nghìn

        // Chuẩn hóa message
        String normalizedMessage = message.toLowerCase().replaceAll("\\s+", "");

        // Từ khóa liên quan đến danh mục sản phẩm
        String[] categoriesName = {
                "áonam", "quầnnam", "sơmi", "coffee", "tết", "routinesquad", "áolennam",
                "quầnnỉnam", "áothun", "áonỉ", "jean", "short", "polo", "áokhoác", "quầntây",
                "độlót", "sơmi", "áovest", "kaki", "phụkiện", "tanktop", "jogger"
        };

        // Kiểm tra có chứa danh mục không
        boolean containsCategory = Arrays.stream(categoriesName)
                .anyMatch(normalizedMessage::contains);

        return containsPriceKeywords && containsCategory;
    }


    @GetMapping("/chatbot")
    public ModelAndView chatbotPage(Principal principal, Model model) {
        Long userId = null;

        // Lấy danh sách sản phẩm
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Cập nhật giá sản phẩm với giảm giá nếu có
        for (Product product : products) {
            BigDecimal effectivePrice = discountService.getActiveDiscountForProduct(product)
                    .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());
            product.setEffectivePrice(effectivePrice);
        }

        Map<Long, List<String>> productImages = new HashMap<>();
        // Nhóm danh sách ảnh theo productId
        for (Product product : products) {
            List<Image> images = imageService.findImagesByProductVariantId(product.getId());
            if (images != null && !images.isEmpty()) {
                List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
                productImages.put(product.getId(), imageUrls);
            }
        }
        model.addAttribute("productImages", productImages);

        Map<Long, Map<String, Integer>> productSizeQuantities = new HashMap<>();
        Map<Long, List<ProductVariant>> productVariants = new HashMap<>();

        for (Product product : products) {
            List<ProductVariant> variants = product.getVariants(); // Lấy tất cả các variants của sản phẩm
            productVariants.put(product.getId(), variants);

            Map<String, Integer> sizeQuantities = new HashMap<>();
            for (ProductVariant variant : variants) {
                // Lấy danh sách kích thước từ sizeService thay vì từ variant.getSizes()
                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                if (sizes != null) {
                    for (Size size : sizes) {
                        sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
                    }
                }
            }
            productSizeQuantities.put(product.getId(), sizeQuantities);
        }


        model.addAttribute("productVariants", productVariants);
        model.addAttribute("productSizeQuantities", productSizeQuantities);

        // Lọc các sản phẩm khuyến mãi (Pre-sale)
        List<Product> preSaleProducts = products.stream()
                .filter(p -> p.getEffectivePrice().compareTo(p.getPrice()) < 0)
                .sorted(Comparator.comparing(Product::getEffectivePrice))
                .limit(10)
                .collect(Collectors.toList());
        model.addAttribute("preSaleProducts", preSaleProducts);

        // Xử lý principal và user
        if (principal != null) {
            System.out.println("Principal Name: " + principal.getName());

            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                userId = user.getId();
                model.addAttribute("user", user);
            } else {
                System.out.println("User not found for: " + principal.getName());
            }
        } else {
            System.out.println("Principal is NULL!");
        }

        System.out.println("UserID in chatbotPage: " + userId);

        return new ModelAndView("ai_chatbot/chat-window");
    }
}
