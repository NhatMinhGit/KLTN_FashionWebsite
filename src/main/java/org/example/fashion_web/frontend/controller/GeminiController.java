package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.services.*;
import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CartService cartService;


    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message, Principal principal, Model model, HttpSession session) {
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
        }
        else if (isServiceRecords(message)) {
            response = geminiService.loadServiceLog(user.getId());
        }
        else if (isRefundPolicy(message)){
            response = geminiService.refundPolicyForUser();
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }
        else if (isFAQS(message)){
            response = geminiService.faqShowForUser();
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isBESTSELLERS(message)) {
            response = geminiService.checkTopProductsRevenueForUser(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }
//        else if (isProductSizeAndQuantityQuery(message)) {
//            // Trích xuất thông tin size và số lượng từ message
//            String size = extractSizeFromMessage(message);
//            int quantity = extractQuantityFromMessage(message);
//
//            // Xử lý theo size và quantity
//            response = geminiService.addToCart(user.getId(), productId, size, color, quantity);
//            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
//        }
        else {
            response = geminiService.chatWithAI(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }


        return ResponseEntity.ok(response);
    }
    private boolean isServiceRecords(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("service record") ||
                lowerCaseMessage.contains("service records") ||
                lowerCaseMessage.contains("record");
    }
    // Ví dụ phương thức kiểm tra thông điệp chứa thông tin về size và số lượng
    private boolean isProductSizeAndQuantityQuery(String message) {
        return message.matches(".*(size|kích thước).*\\d+.*(số lượng|mua).*\\d+.*");
    }

    // Trích xuất thông tin size từ thông điệp
    private String extractSizeFromMessage(String message) {
        // Giả sử thông điệp có cấu trúc như "size: M" hoặc "kích thước: L"
        if (message.contains("size")) {
            // Sử dụng regex hoặc phương pháp khác để trích xuất size
            return message.replaceAll(".*(size|kích thước):\\s*(\\w+).*", "$2");
        }
        return null;
    }

    // Trích xuất số lượng từ thông điệp
    private int extractQuantityFromMessage(String message) {
        // Ví dụ: trích xuất số lượng như "2 sản phẩm" hoặc "Mua 3"
        Pattern pattern = Pattern.compile("(\\d+)\\s*(sản phẩm|cái|mua)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0; // Nếu không tìm thấy, trả về số lượng mặc định
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

    private boolean isBESTSELLERS(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("best sellers") ||
                lowerCaseMessage.contains("bán chạy") ||
                lowerCaseMessage.contains("được yêu thích") ||
                lowerCaseMessage.contains("trending");
    }
    private boolean isRefundPolicy(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("refund policy") ||
                lowerCaseMessage.contains("hoàn phí") ||
                lowerCaseMessage.contains("chính sách hoàn phí") ||
                lowerCaseMessage.contains("chính sách đổi trả") ||
                lowerCaseMessage.contains("refund");
    }
    private boolean isFAQS(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return  lowerCaseMessage.contains("faqs") ||
                lowerCaseMessage.contains("thắc mắc") ||
                lowerCaseMessage.contains("sử dụng") ||
                lowerCaseMessage.contains("hướng dẫn") ||
                lowerCaseMessage.contains("hướng dẫn sử dụng");
    }

    private boolean isOrderRequest(String message) {
        String lowerCaseMessage = message.toLowerCase();
        return lowerCaseMessage.startsWith("đặt hàng") ||
                lowerCaseMessage.startsWith("mua") ||
                lowerCaseMessage.contains("tôi muốn mua") ||
                lowerCaseMessage.contains("tôi muốn đặt");
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
    public ModelAndView chatbotPage(Principal principal, Model model,HttpSession session) {
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

        // Map ảnh theo variantId thay vì productId
        Map<Long, List<String>> imagesByVariant = new HashMap<>();

        for (Product product : products) {
            List<ProductVariant> variants = product.getVariants();
            if (variants != null) {
                for (ProductVariant variant : variants) {
                    List<Image> images = imageService.findImagesByProductVariantId(variant.getId());
                    if (images != null && !images.isEmpty()) {
                        List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
                        imagesByVariant.put(variant.getId(), imageUrls);
                    }
                }
            }
        }
        model.addAttribute("imagesByVariant", imagesByVariant);

        // Map size - số lượng tồn kho theo variant
        Map<Long, Map<String, Integer>> productSizeQuantities = new HashMap<>();
        Map<Long, List<ProductVariant>> productVariants = new HashMap<>();

        for (Product product : products) {
            List<ProductVariant> variants = product.getVariants();
            productVariants.put(product.getId(), variants);

            Map<String, Integer> sizeQuantities = new HashMap<>();
            for (ProductVariant variant : variants) {
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

        // Lọc các sản phẩm khuyến mãi
        // Lấy giảm giá cho sản phẩm
        for (Product product : products) {
            List<ProductDiscount> productDiscounts = discountService.getActiveDiscountsForProduct(product);
            List<ProductDiscount> categoryDiscounts = discountService.getActiveDiscountsForCategory(product.getCategory());

            // Nếu không có giảm giá, giữ nguyên giá gốc
            if (productDiscounts.isEmpty() && categoryDiscounts.isEmpty()) {
                product.setEffectivePrice(product.getPrice());
                continue;
            }

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
                    .orElse(product.getPrice()); // Nếu không có giảm giá, sử dụng giá gốc

            // Cập nhật giá hiệu lực cho sản phẩm
            product.setEffectivePrice(effectivePrice);
        }

// Lọc các sản phẩm khuyến mãi (đã có giá hiệu lực hợp lệ)
        List<Product> preSaleProducts = products.stream()
                .filter(p -> p.getPrice() != null
                        && p.getEffectivePrice() != null
                        && p.getPrice().compareTo(BigDecimal.ZERO) > 0
                        && p.getEffectivePrice().compareTo(BigDecimal.ZERO) >= 0
                        && p.getEffectivePrice().compareTo(p.getPrice()) < 0) // Chỉ chọn sản phẩm có giảm giá
                .sorted(Comparator.comparing((Product p) ->
                        p.getPrice().subtract(p.getEffectivePrice())
                                .divide(p.getPrice(), 2, RoundingMode.HALF_UP) // Tính tỷ lệ giảm giá
                ).reversed()) // Sắp xếp giảm dần
                .limit(10) // Giới hạn 10 sản phẩm đầu tiên
                .collect(Collectors.toList()); // Thu thập thành danh sách

        model.addAttribute("preSaleProducts", preSaleProducts); // Đưa vào model để hiển thị trên giao diện





        // Xử lý principal
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                userId = user.getId();
                model.addAttribute("user", user);
            }
        }
        List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");
        System.out.println(cart);
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

        model.addAttribute("productImages", productImages);
        model.addAttribute("cartItems", cart);
        System.out.println("Items cart sau khi load trang cart: " + cart);

        return new ModelAndView("ai_chatbot/chat-window");
    }

}
