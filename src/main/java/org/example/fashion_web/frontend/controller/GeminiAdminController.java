package org.example.fashion_web.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.ChatbotSession;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.services.ChatbotService;
import org.example.fashion_web.backend.services.ChatbotSessionService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
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

    @Autowired
    private ChatbotSessionService chatbotSessionService;


    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message, Principal principal) throws JsonProcessingException {
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

        ChatbotSession session = chatbotSessionService.startOrGetActiveSession(chatbot, user.getId());

        // Lưu message người dùng gửi
        geminiService.saveConversation(session, "USER", message, "text", null, null);

        // Trích xuất intent và entities
        Map<String, String> extractedData = extractIntentAndEntities(message);
        String intent = extractedData.get("intent");
        String entities = extractedData.get("entities"); // Hoặc nếu bạn không cần entities, có thể là null

        // Xử lý phản hồi dựa trên intent
        switch (intent) {
            case "check_stock":
                response = geminiService.checkStock(message);
                break;
            case "refund_policy":
                response = geminiService.refundPolicyForStaff();
                break;
            case "faq":
                response = geminiService.faqShowForStaff();
                break;
            case "monthly_revenue":
                response = geminiService.checkMonthlyRenvenue(message);
                break;
            case "yearly_revenue":
                response = geminiService.checkYearlyRenvenue(message);
                break;
            case "business_plan": {
                Map<String, Object> businessPlanResponse = new HashMap<>();

                // 1. Top sản phẩm doanh thu cao
                String topProducts = geminiService.checkTopProductsRevenueForOptimalPlan(message);
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
                response = geminiService.checkTopProductsRevenueForUser(message);
                break;
            default:
                response = geminiService.chatWithAI(message);
                break;
        }

        // Lưu phản hồi từ chatbot
        geminiService.saveConversation(session, "BOT", response, "text", intent, entities);

        return ResponseEntity.ok(response);
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
        } else if (isYearlyRevenue(lowerCaseMessage)) {
            result.put("intent", "yearly_revenue");
        } else if (isOptimalBusinessPlan(lowerCaseMessage)) {
            result.put("intent", "business_plan");
        } else if (isBESTSELLERS(lowerCaseMessage)) {
            result.put("intent", "check_bestsellers");
        } else {
            result.put("intent", "general_chat");
        }

        // Trích xuất các thực thể (entities) từ câu hỏi (có thể mở rộng thêm theo yêu cầu)
        result.putAll(extractEntities(lowerCaseMessage));

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


    private boolean isStockQuery(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("còn không") ||
                lowerCaseMessage.contains("còn hàng không") ||
                lowerCaseMessage.contains("có hàng không") ||
                lowerCaseMessage.contains("số lượng");

    }
    private boolean isMonthlyRevenue(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("doanh thu") ||
                lowerCaseMessage.contains("tình hình tháng này") ||
                lowerCaseMessage.contains("tháng này") ||
                lowerCaseMessage.contains("this month revenue") ||
                lowerCaseMessage.contains("phân tích doanh thu");
    }

    private boolean isYearlyRevenue(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("doanh thu") ||
                lowerCaseMessage.contains("tình hình năm này") ||
                lowerCaseMessage.contains("năm này") ||
                lowerCaseMessage.contains("this year revenue") ||
                lowerCaseMessage.contains("phân tích doanh thu");
    }
    private boolean isOptimalBusinessPlan(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("chiến lược") ||
                lowerCaseMessage.contains("bussiness plan") ||
                lowerCaseMessage.contains("optimal bussiness plan") ||
                lowerCaseMessage.contains("tương lai") ||
                lowerCaseMessage.contains("phân tích chiến lược") ||
                lowerCaseMessage.contains("tăng doanh số");
    }

    @GetMapping("/chatbot")
    public ModelAndView chatbotPage(Principal principal, Model model) {
        Long userId = null;

        if (principal != null) {
            System.out.println("Principal Name: " + principal.getName());  // Debug principal

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

        System.out.println("UserID in chatbotPage: " + userId); // Debug userId

        return new ModelAndView("ai_chatbot/admin-chat-window");
    }
}
