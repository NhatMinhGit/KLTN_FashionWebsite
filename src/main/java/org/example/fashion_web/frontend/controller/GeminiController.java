package org.example.fashion_web.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
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
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
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

    @Autowired
    private OrderService orderService;



    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message, Principal principal) {
        String response;
        String userName = principal.getName();
        User user = userService.findByEmail(userName);

        if (user == null) {
            return ResponseEntity.badRequest().body("Người dùng không tồn tại.");
        }

        Chatbot chatbot = chatbotService.findChatBotByUserId(user.getId());
        if (chatbot == null) {
            return ResponseEntity.badRequest().body("Chatbot không tồn tại cho người dùng này.");
        }

        // Lưu message người dùng gửi
        geminiService.saveConversation(chatbot, "USER", message, "text", null, null);

        // Trích xuất intent và entities
        Map<String, String> extractedData = extractIntentAndEntities(message);
        String intent = extractedData.get("intent");
        String entities = extractedData.get("entities"); // Hoặc nếu bạn không cần entities, có thể là null

        // Kiểm tra và xử lý theo intent
        switch (intent) {
            case "stock_query":
                response = geminiService.checkStock(message);
                break;
            case "price_query":
                response = geminiService.checkPriceAndCategory(message);
                break;
            case "refund_policy":
                response = geminiService.refundPolicyForUser();
                break;
            case "faqs":
                response = geminiService.faqShowForUser();
                break;
            case "bestsellers":
                response = geminiService.checkTopProductsRevenueForUser(message);
                break;
            case "shipping_issue":
                response = geminiService.shippingIssueResponseFoUser();
                break;
            case "payment_method_change":
                response = geminiService.paymentMethodChangeInstructions();
                break;
            case "payment_declined_reason":
                response = geminiService.paymentDeclinedReasonResponse();
                break;
            case "delivery_time":
                response = geminiService.deliveryTimeResponse();
                break;
            case "order_tracking":
                response = geminiService.orderTrackingResponse(message,principal);
                break;
            case "return_policy":
                response = geminiService.returnPolicyResponse();
                break;
            case "product_issue":
                response = geminiService.productIssueResponse();
                break;
            case "refund_time":
                response = geminiService.refundTimeResponse();
                break;
            case "change_product_model":
                response = geminiService.changeProductModelResponse();
                break;
            default:
                response = geminiService.chatWithAI(message);
                break;
        }

        geminiService.saveConversation(chatbot, "BOT", response, "text", intent, entities);

        return ResponseEntity.ok(response);
    }

    // Hàm trích xuất intent và entities từ câu hỏi
    private Map<String, String> extractIntentAndEntities(String message) {
        Map<String, String> result = new HashMap<>();
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra intent
        if (isStockQuery(lowerCaseMessage)) {
            result.put("intent", "stock_query");
        } else if (isPrice(lowerCaseMessage)) {
            result.put("intent", "price_query");
        } else if (isRefundPolicy(lowerCaseMessage)) {
            result.put("intent", "refund_policy");
        } else if (isFAQS(lowerCaseMessage)) {
            result.put("intent", "faq");
        } else if (isBESTSELLERS(lowerCaseMessage)) {
            result.put("intent", "bestsellers");
        } else if (isShippingDelayed(lowerCaseMessage)) {
            result.put("intent", "shipping_issue");
        } else if (isPaymentMethodChange(lowerCaseMessage)) {
            result.put("intent", "payment_method_change");
        } else if (isPaymentDeclinedReason(lowerCaseMessage)) {
            result.put("intent", "payment_declined_reason");
        } else if (isDeliveryTime(lowerCaseMessage)) {
            result.put("intent", "delivery_time");
        } else if (isOrderTracking(lowerCaseMessage)) {
            result.put("intent", "order_tracking");
        } else if (isReturnPolicyQuestion(lowerCaseMessage)) {
            result.put("intent", "return_policy");
        } else if (isProductIssueQuestion(lowerCaseMessage)) {
            result.put("intent", "product_issue");
        } else if (isRefundTimeQuestion(lowerCaseMessage)) {
            result.put("intent", "refund_time");
        } else if (isChangeProductModelQuestion(lowerCaseMessage)) {
            result.put("intent", "change_product_model");
        } else {
            result.put("intent", "general_chat"); // Intent mặc định nếu không xác định được
        }

        // Trích xuất các thực thể (entities) từ câu hỏi (dùng message gốc)
        Map<String, String> extractedEntities = extractEntities(message);

        // Thêm thực thể vào kết quả dưới khóa "entities"
        result.put("entities", extractedEntities.toString());

        return result;
    }


    // Hàm trích xuất entities từ câu hỏi
    private Map<String, String> extractEntities(String message) {
        Map<String, String> entities = new HashMap<>();
        String lowerCaseMessage = message.toLowerCase();

        // Trích xuất loại sản phẩm (category) từ câu hỏi
        if (lowerCaseMessage.contains("áo")) {
            entities.put("product_category", "áo");
        }
        if (lowerCaseMessage.contains("quần")) {
            entities.put("product_category", "quần");
        }

        // Trích xuất giá từ câu hỏi (ví dụ: "500 nghìn", "3 triệu")
        Pattern pricePattern = Pattern.compile(".*\\d+\\s*(k|nghìn|triệu).*");
        Matcher priceMatcher = pricePattern.matcher(lowerCaseMessage);
        if (priceMatcher.matches()) {
            entities.put("price", priceMatcher.group(0)); // Thực thể giá trị
        }

        // Trích xuất size nếu có
        if (lowerCaseMessage.contains("size m")) {
            entities.put("size", "M");
        }

        // Trích xuất tháng (ví dụ: tháng 5, tháng 11)
        Pattern monthPattern = Pattern.compile("(tháng\\s*(\\d{1,2}))|(\\b(0?[1-9]|1[0-2])\\b)");
        Matcher monthMatcher = monthPattern.matcher(lowerCaseMessage);
        if (monthMatcher.find()) {
            String month = monthMatcher.group(2) != null ? monthMatcher.group(2) : monthMatcher.group(1);
            entities.put("month", month.replaceAll("[^\\d]", ""));
        }

        // Trích xuất năm (ví dụ: năm 2025, năm 2020)
        Pattern yearPattern = Pattern.compile("năm\\s*(\\d{4})|(\\b20\\d{2}\\b)");
        Matcher yearMatcher = yearPattern.matcher(lowerCaseMessage);
        if (yearMatcher.find()) {
            String year = yearMatcher.group(1) != null ? yearMatcher.group(1) : yearMatcher.group(2);
            entities.put("year", year);
        }

        return entities;
    }


    // Các phương thức kiểm tra như trước
    private boolean isStockQuery(String message) {
        return containsKeywords(message, "còn không", "còn hàng không", "có hàng không", "số lượng");
    }

    private boolean isPrice(String message) {
        return containsKeywords(message, "giá", "bao nhiêu tiền", "khoảng", "tầm") ||
                message.toLowerCase().matches(".*\\d+.*k.*") ||
                message.toLowerCase().matches(".*\\d+.*nghìn.*");
    }
    private boolean isDeliveryTime(String message) {
        return containsKeywords(message,
                "giao trong bao lâu",
                "bao lâu thì nhận được",
                "thời gian giao hàng",
                "khi nào nhận được hàng",
                "giao hàng mất mấy ngày",
                "mất bao lâu để giao");
    }
    private boolean isOrderTracking(String message) {
        return containsKeywords(message,
                "kiểm tra đơn hàng",
                "theo dõi đơn hàng",
                "đơn hàng đang ở đâu",
                "track đơn hàng",
                "tra cứu vận đơn",
                "check order",
                "đơn hàng đã giao chưa",
                "track");
    }

    private boolean isShippingDelayed(String message) {
        return containsKeywords(message,
                "chưa nhận được hàng",
                "hàng chưa tới",
                "giao hàng chưa đến",
                "chưa giao hàng",
                "đợi mãi chưa thấy hàng",
                "sao hàng chưa về",
                "không nhận được hàng",
                "khi nào giao hàng"
        );
    }
    private boolean isPaymentDeclinedReason(String message) {
        return containsKeywords(message,
                "tại sao thanh toán bị từ chối",
                "thanh toán bị từ chối",
                "không thanh toán được",
                "lỗi thanh toán",
                "không trả tiền được",
                "thanh toán thất bại");
    }

    private boolean isRefundPolicy(String message) {
        return containsKeywords(message, "refund policy", "hoàn phí", "chính sách hoàn phí", "chính sách đổi trả", "refund");
    }

    private boolean isFAQS(String message) {
        return containsKeywords(message, "faqs", "thắc mắc", "sử dụng", "hướng dẫn", "hướng dẫn sử dụng");
    }

    private boolean isBESTSELLERS(String message) {
        return containsKeywords(message, "best sellers", "bán chạy", "được yêu thích", "trending");
    }
    private boolean isPaymentMethodChange(String message) {
        return containsKeywords(message,
                "đổi phương thức thanh toán",
                "thay đổi cách thanh toán",
                "thay đổi phương thức trả tiền",
                "thay đổi phương thức",
                "có thể đổi thanh toán không",
                "đổi thanh toán");
    }
    // Ý định 1: Chính sách đổi trả
    private boolean isReturnPolicyQuestion(String message) {
        return containsKeywords(message,
                "chính sách đổi trả", "đổi trả", "chính sách hoàn hàng", "đổi hàng", "trả hàng");
    }

    // Ý định 2: Sản phẩm bị lỗi hoặc không đúng mô tả
    private boolean isProductIssueQuestion(String message) {
        return containsKeywords(message,
                "sản phẩm bị lỗi", "không đúng mô tả", "hư hỏng", "lỗi sản phẩm", "sai mô tả");
    }

    // Ý định 3: Thời gian hoàn tiền
    private boolean isRefundTimeQuestion(String message) {
        return containsKeywords(message,
                "thời gian hoàn tiền", "hoàn tiền mất bao lâu", "khi nào nhận được tiền", "bao lâu hoàn tiền");
    }

    // Ý định 4: Đổi sang mẫu khác
    private boolean isChangeProductModelQuestion(String message) {
        return containsKeywords(message,
                "đổi sang mẫu khác", "đổi mẫu", "đổi size", "đổi màu", "thay sản phẩm");
    }

    private boolean containsKeywords(String message, String... keywords) {
        String lowerCaseMessage = message.toLowerCase();
        for (String keyword : keywords) {
            if (lowerCaseMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }


    private String convertEntitiesToJson(Map<String, String> entities) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(entities); // Chuyển đổi Map thành chuỗi JSON
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi chuyển đổi entities thành JSON: " + e.getMessage() + "\"}";
        }
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





        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                userId = user.getId();
                model.addAttribute("user", user);

                // Tìm hoặc tạo mới Chatbot theo userId
                Chatbot chatbot = chatbotService.findChatBotByUserId(userId);
                if (chatbot == null) {
                    chatbot = new Chatbot();
                    chatbot.setName("Chatbot người dùng " + user.getName());
                    chatbot.setUserId(userId.intValue());
                    chatbot.setStatus("active");
                    chatbot.setCreatedAt(LocalDateTime.now());
                    chatbotService.save(chatbot);
                    System.out.println("Đã tạo mới chatbot cho userId: " + userId);
                } else {
                    System.out.println("Đã tìm thấy chatbot cho userId: " + userId);
                }

                model.addAttribute("chatbot", chatbot);
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



        // Lấy danh sách đơn hàng đã giao hoặc đang giao
        User user = userService.findByEmail(principal.getName());
        List<Order> trackingOrders = orderService.findOrdersByUserAndStatusIn(
                user,
                Arrays.asList(Order.OrderStatusType.SHIPPED, Order.OrderStatusType.COMPLETED)
        );
        model.addAttribute("trackingOrders", trackingOrders != null ? trackingOrders : new ArrayList<>());

        // Lấy danh sách đơn hàng đang chờ được xử lý
        List<Order> pendingOrders = orderService.findOrdersByUserAndStatusIn(
                user,
                Arrays.asList(Order.OrderStatusType.PENDING)
        );
        model.addAttribute("pendingOrders", pendingOrders != null ? pendingOrders : new ArrayList<>());

        return new ModelAndView("ai_chatbot/chat-window");
    }

}
