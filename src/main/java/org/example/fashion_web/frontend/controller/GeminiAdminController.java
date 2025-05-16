package org.example.fashion_web.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.services.ChatbotService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class GeminiAdminController {
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatbotService chatbotService;



    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message, Principal principal) throws JsonProcessingException {
        try{
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
            boolean isAdmin = user.getRole().equalsIgnoreCase("ADMIN");
            // Xử lý phản hồi dựa trên intent
            switch (intent) {
                case "check_stock":
                    response = geminiService.checkStock(message,isAdmin);
                    break;
                case "refund_policy":
                    response = geminiService.refundPolicyForStaff();
                    break;
                case "faq":
                    response = geminiService.faqShowForStaff();
                    break;
                case "monthly_revenue":
                    response = geminiService.checkMonthlyRevenue(message);
                    break;
                case "yearly_revenue":
                    response = geminiService.checkYearlyRenvenue(message);
                    break;
                case "order_tracking":
                    response = geminiService.orderTrackingResponse(message,principal);
                    break;
                case "technical_support":
                    response = geminiService.technicalSupportForStaffResponse();
                    break;
                case "business_plan": {
                    Map<String, Object> businessPlanResponse = new HashMap<>();

                    // 1. Top sản phẩm doanh thu cao
                    String topProducts = geminiService.checkTopProductsRevenueForOptimalPlan(message,isAdmin);
                    businessPlanResponse.put("topProducts", topProducts);

                    // 2. Gợi ý thời trang theo mùa
                    String weatherSuggestionJson = geminiService.getVietnamWeatherSuggestion(message);

                    // 3. Gợi ý thời trang theo sự kiện
                    String eventSuggestionJson = geminiService.getVietnamEventSuggestion(message);

                    ObjectMapper objectMapper = new ObjectMapper();

                    String aiResponse;
                    try {
                        // Parse thời trang theo mùa
                        Map<String, Object> weatherData = objectMapper.readValue(weatherSuggestionJson, Map.class);
                        String currentSeason = (String) weatherData.get("currentSeason");
                        List<String> currentSuggestions = ((List<?>) weatherData.get("currentSuggestions"))
                                .stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());


                        String nextSeason = (String) weatherData.get("nextSeason");
                        List<String> nextSuggestions = ((List<?>) weatherData.get("nextSuggestions"))
                                .stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());

                        // Parse thời trang theo sự kiện
                        Map<String, Object> eventData = objectMapper.readValue(eventSuggestionJson, Map.class);
                        String eventSuggestion = (String) eventData.get("suggestion");

                        // Format lại aiResponse
                        aiResponse = "Đây là kế hoạch doanh nghiệp của bạn:<br><br>" +
                                "🌤️ Gợi ý thời trang theo mùa hiện tại (" + currentSeason + "): " + String.join(", ", currentSuggestions) + "<br>" +
                                "🍂 Chuẩn bị cho mùa tiếp theo (" + nextSeason + "): " + String.join(", ", nextSuggestions) + "<br>" +
                                "🎉 Gợi ý thời trang theo sự kiện: " + eventSuggestion;

                    } catch (Exception e) {
                        aiResponse = "Đây là kế hoạch doanh nghiệp của bạn, nhưng có lỗi khi xử lý gợi ý thời tiết hoặc sự kiện.";
                    }

                    businessPlanResponse.put("aiResponse", aiResponse);
                    Map<String, String> productInfoMap = objectMapper.readValue(topProducts, new TypeReference<Map<String, String>>() {});
                    String productInfoHtml = productInfoMap.get("productInfo");
                    businessPlanResponse.put("productInfo", productInfoHtml);



                    try {
                        response = new ObjectMapper().writeValueAsString(businessPlanResponse);
                    } catch (JsonProcessingException e) {
                        response = "{\"error\": \"Lỗi tạo JSON phản hồi: " + e.getMessage() + "\"}";
                    }
                    break;
                }

                case "check_bestsellers":
                    response = geminiService.checkTopProductsRevenueForUser(message,isAdmin);
                    break;
                default:

                    response = geminiService.chatWithAI(message,isAdmin);
                    break;
            }

            // Lưu phản hồi từ chatbot
            geminiService.saveConversation(chatbot, "BOT", response, "text", intent, entities);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Ghi log lỗi (nếu dùng logger)
            e.printStackTrace();
            // Trả về lỗi cho client
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Chat bot đang trong quá trình nên có một số lỗi và các tính năng chưa được khả dụng. Mong quý khách thông cảm !!!");
        }
    }

    // Hàm trích xuất intent và entities từ câu hỏi
    private Map<String, String> extractIntentAndEntities(String message) {
        Map<String, String> result = new HashMap<>();
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra intent
        if (isStockQuery(lowerCaseMessage)) {
            result.put("intent", "check_stock");
        } else if (isRefundPolicy(lowerCaseMessage)) {
            result.put("intent", "refund_policy");
        } else if (isFAQS(lowerCaseMessage)) {
            result.put("intent", "faq");
        } else if (isMonthlyRevenue(lowerCaseMessage)) {
            result.put("intent", "monthly_revenue");
        } else if (isOrderTracking(lowerCaseMessage)) {
            result.put("intent", "order_tracking");
        } else if (isYearlyRevenue(lowerCaseMessage)) {
            result.put("intent", "yearly_revenue");
        } else if (isTechnicalSupport(lowerCaseMessage)) {
            result.put("intent", "technical_support");
        } else if (isOptimalBusinessPlan(lowerCaseMessage)) {
            result.put("intent", "business_plan");
        } else if (isBESTSELLERS(lowerCaseMessage)) {
            result.put("intent", "check_bestsellers");
        } else {
            result.put("intent", "general_chat");
        }

        // Trích xuất các thực thể (entities) từ câu hỏi (dùng message gốc)
        Map<String, String> extractedEntities = extractEntities(message);

        // Thêm thực thể vào kết quả dưới khóa "entities"
        result.put("entities", extractedEntities.toString());

        return result;
    }

    // Hàm trích xuất entities (có thể mở rộng thêm)
    private Map<String, String> extractEntities(String message) {
        Map<String, String> entities = new HashMap<>();
        // Ví dụ: Trích xuất tháng hoặc năm từ câu hỏi (có thể thêm các entities khác)
        Pattern monthPattern = Pattern.compile("(tháng\\s*(\\d{1,2}))|(\\b(0?[1-9]|1[0-2])\\b)");
        Matcher monthMatcher = monthPattern.matcher(message);
        if (monthMatcher.find()) {
            String month = monthMatcher.group(2) != null ? monthMatcher.group(2) : monthMatcher.group(1);
            entities.put("month", month.replaceAll("[^\\d]", ""));
        }

        Pattern yearPattern = Pattern.compile("năm\\s*(\\d{4})|(\\b20\\d{2}\\b)");
        Matcher yearMatcher = yearPattern.matcher(message);
        if (yearMatcher.find()) {
            String year = yearMatcher.group(1) != null ? yearMatcher.group(1) : yearMatcher.group(2);
            entities.put("year", year);
        }

        return entities;
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
    private boolean isBESTSELLERS(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"best sellers"
                ,"bán chạy"
                ,"được yêu thích"
                ,"trending");
    }
    private boolean isTechnicalSupport(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"technical support"
                ,"hỗ trợ kỹ thuật"
                ,"lỗi hệ thống");
    }
    private boolean isRefundPolicy(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"refund policy"
                ,"hoàn phí"
                ,"chính sách hoàn phí"
                ,"refund");
    }
    private boolean isFAQS(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"faqs"
                ,"thắc mắc"
                ,"sử dụng"
                ,"hướng dẫn"
                ,"hướng dẫn sử dụng");
    }


    private boolean isStockQuery(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"còn không"
                ,"còn hàng không"
                ,"có hàng không"
                ,"số lượng");

    }
    private boolean isMonthlyRevenue(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message
                ,"tình hình tháng này"
                ,"tháng này"
                ,"this month revenue"
                ,"phân tích doanh thu");
    }

    private boolean isYearlyRevenue(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"doanh thu"
                ,"tình hình năm này"
                ,"năm này"
                ,"this year revenue"
                ,"doanh thu năm nay");
    }
    private boolean isOptimalBusinessPlan(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        return containsKeywords(message,"chiến lược"
                ,"bussiness plan"
                ,"optimal bussiness plan"
                ,"tương lai"
                ,"phân tích chiến lược"
                ,"tăng doanh số");
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
    @GetMapping("/chatbot")
    public ModelAndView chatbotPage(Principal principal, Model model) {
        Long userId = null;

        if (principal != null) {
            System.out.println("Principal Name: " + principal.getName());

            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                userId = user.getId();
                model.addAttribute("user", user);

                // Tìm chatbot của người dùng
                Chatbot chatbot = chatbotService.findChatBotByUserId(userId);

                if (chatbot == null) {
                    // Nếu chưa có -> tạo mới
                    chatbot = new Chatbot();
                    chatbot.setName("Chatbot nhân viên " + user.getName());  // hoặc user.getEmail()
                    chatbot.setUserId(userId.intValue());
                    chatbot.setStatus("active");
                    chatbot.setCreatedAt(LocalDateTime.now());

                    chatbotService.save(chatbot);
                    System.out.println("Tạo mới chatbot cho userId: " + userId);
                } else {
                    System.out.println("Đã tồn tại chatbot cho userId: " + userId);
                }

                model.addAttribute("chatbot", chatbot);
            } else {
                System.out.println("User not found for: " + principal.getName());
            }
        } else {
            System.out.println("Principal is NULL!");
        }

        return new ModelAndView("ai_chatbot/admin-chat-window");
    }

}
