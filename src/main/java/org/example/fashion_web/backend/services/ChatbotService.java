package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Chatbot;

public interface ChatbotService {
    Chatbot findChatBotByUserId(Long userId);

    Chatbot save(Chatbot chatbot);
}
