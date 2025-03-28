package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.services.ChatbotService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

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
