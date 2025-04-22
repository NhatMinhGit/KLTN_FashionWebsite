package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.ChatbotService;
import org.example.fashion_web.backend.services.UserService;
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

import java.security.Principal;
import java.util.Arrays;

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
                "quầnnỉnam", "áothun", "áonỉ", "jean","short","polo","áokhoác","quầntây",
                "độlót", "sơmi", "áovest", "kaki","phụkiện","tanktop", "jogger"
        };

        // Kiểm tra có chứa danh mục không
        boolean containsCategory = Arrays.stream(categoriesName)
                .anyMatch(normalizedMessage::contains);

        return containsPriceKeywords && containsCategory;
    }



    @GetMapping("/chatbot")
    public ModelAndView chatbotPage(Principal principal,Model model) {
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

        return new ModelAndView("chat-window");
    }
}
