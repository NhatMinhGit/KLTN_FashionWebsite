package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.ChatbotSession;
import org.example.fashion_web.backend.services.ChatbotSessionService;
import org.springframework.stereotype.Service;

@Service
public class ChatbotSessionServiceImpl implements ChatbotSessionService {
    @Override
    public ChatbotSession startOrGetActiveSession(Chatbot chatbot, Long userId) {
        return null;
    }
}
