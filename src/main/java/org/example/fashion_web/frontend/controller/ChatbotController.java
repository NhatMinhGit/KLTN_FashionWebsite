package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.services.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {
    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/message")
    public String chat(@RequestBody String userMessage) throws IOException {
        return chatbotService.processMessage(userMessage);
    }
}