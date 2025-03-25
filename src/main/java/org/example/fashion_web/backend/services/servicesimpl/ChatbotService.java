package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.example.fashion_web.backend.services.servicesimpl.QdrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class ChatbotService {
    @Autowired
    private GeminiService geminiService;

    @Autowired
    private QdrantService qdrantService;

    public String processMessage(String userMessage) throws IOException {
        // Gửi tin nhắn đến Gemini API
        String response = geminiService.getGeminiResponse(userMessage);

        // Giả sử ta có một vector đại diện cho câu trả lời (cần dùng mô hình embedding thực tế)
        double[] vector = {0.1, 0.2, 0.3, 0.4, 0.5}; // Placeholder

        // Lưu phản hồi vào Qdrant
        qdrantService.insertVector("chat_responses", userMessage.hashCode(), vector);

        return response;
    }
}
