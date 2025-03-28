package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Chatbot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotRepository extends JpaRepository<Chatbot, Long> {
    // findChatbotByUserId
    Chatbot findByUserId(Long userId);
}
