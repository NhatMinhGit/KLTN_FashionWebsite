package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    // Xử lý tin nhắn gửi từ người dùng và gửi tới kênh admin
    @MessageMapping("/chat.sendMessageUser")
    @SendTo("/topic/admin")  // Gửi tin nhắn tới tất cả các admin đã đăng ký
    public ChatMessage sendMessageFromUser(@Payload ChatMessage message) {
        //System.out.println("Nhận tin nhắn: " + message.getContent());
        return message;
    }

    // Xử lý tin nhắn gửi từ admin và gửi tới kênh người dùng
    @MessageMapping("/chat.sendMessageAdmin")
    @SendTo("/topic/user")  // Gửi tin nhắn tới tất cả người dùng đã đăng ký
    public ChatMessage sendMessageFromAdmin(@Payload ChatMessage message) {
        //System.out.println("Nhận tin nhắn: " + message.getContent());
        return message;
    }


}

