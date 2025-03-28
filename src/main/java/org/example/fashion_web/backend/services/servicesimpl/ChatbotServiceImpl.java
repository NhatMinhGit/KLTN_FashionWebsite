package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.repositories.ChatbotRepository;
import org.example.fashion_web.backend.services.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    @Autowired
    private ChatbotRepository chatbotRepository;

    @Override
    public Chatbot findChatBotByUserId(Long userId) {
        return chatbotRepository.findByUserId(userId);
    }
}
