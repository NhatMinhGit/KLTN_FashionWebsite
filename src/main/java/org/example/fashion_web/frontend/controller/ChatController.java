package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.ChatMessage;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;
    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void handleChatMessage(@Payload ChatMessage chatMessage, Model model) {
        // Gửi tin nhắn đến đúng room
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatMessage.getRoomId(), chatMessage);
    }
    @RequestMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userService.findAll(); // hoặc lọc theo vai trò, trạng thái, v.v.
    }



    // Kiểm tra nếu người gửi có quyền gửi tin nhắn đến người nhận
    private boolean isAuthorizedToSend(String sender, String recipient) {
        // Bạn có thể kiểm tra vai trò hoặc quyền hạn của người gửi tại đây
        // Ví dụ: admin có thể gửi tin cho tất cả người dùng
        return true;  // Giả sử tất cả người gửi đều có quyền gửi tin nhắn
    }
}


