package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.services.ChatbotService;
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
    public ResponseEntity<String> chat(@RequestParam String message, Principal principal) {
        String response;
        String userName = principal.getName();
        User user = userService.findByEmail(userName);
        Chatbot chatbot = chatbotService.findChatBotByUserId(user.getId());
        System.out.println("Chatbot ID: " + (chatbot != null ? chatbot.getId() : "null"));
        System.out.println("Id người dùng save vào chatlog "+ user.getId());

        // Kiểm tra nếu người dùng hỏi về số lượng hàng tồn
        if (isStockQuery(message)) {
            response = geminiService.checkStock(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }
        else if (isRefundPolicy(message)){
            response = geminiService.refundPolicyForStaff();
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }
        else if (isFAQS(message)){
            response = geminiService.faqShow();
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isMonthlyRenvenue(message)) {
            response = geminiService.checkMonthlyRenvenue(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isYearlyRenvenue(message)) {
            response = geminiService.checkYearlyRenvenue(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isOptimalBussinessPlan(message)) {
            response =  "<b>1. Nhập thêm các sản phẩm bán chạy như </b>:<br>" + geminiService.checkTopProductsRenvenue(message)
                    +   "<br><b>2. Nhập thêm các sản phẩm tiềm năng theo mùa và sự kiện</b>:<br>"
                    +   "Thời trang theo mùa: <br>" + geminiService.getVietnamWeatherSuggestion(message) + "<br>"
                    +   "Thời trang sự kiện: <br>" + geminiService.getVietnamEventSuggestion(message)
            ;
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else if (isBESTSELLERS(message)) {
            response = geminiService.checkTopProductsRenvenue(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        } else {
            response = geminiService.chatWithAI(message);
            geminiService.saveConversation(user.getId(), "Người dùng: " + message + "\nBot: " + response);
        }

        return ResponseEntity.ok(response);
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
    private boolean isMonthlyRenvenue(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("doanh thu") ||
                lowerCaseMessage.contains("tình hình tháng này") ||
                lowerCaseMessage.contains("tháng này") ||
                lowerCaseMessage.contains("this month revenue") ||
                lowerCaseMessage.contains("phân tích doanh thu");
    }

    private boolean isYearlyRenvenue(String message) {
        // Chuyển câu hỏi về dạng chữ thường để so sánh dễ hơn
        String lowerCaseMessage = message.toLowerCase();

        // Kiểm tra nếu câu hỏi có chứa các từ khóa liên quan đến số lượng tồn kho
        return lowerCaseMessage.contains("doanh thu") ||
                lowerCaseMessage.contains("tình hình năm này") ||
                lowerCaseMessage.contains("năm này") ||
                lowerCaseMessage.contains("this year revenue") ||
                lowerCaseMessage.contains("phân tích doanh thu");
    }
    private boolean isOptimalBussinessPlan(String message) {
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
