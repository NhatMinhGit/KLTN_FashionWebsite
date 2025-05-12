package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Chatbot;
import org.example.fashion_web.backend.models.ChatbotSession;

public interface ChatbotSessionService {
    ChatbotSession startOrGetActiveSession(Chatbot chatbot,Long userId);

}
