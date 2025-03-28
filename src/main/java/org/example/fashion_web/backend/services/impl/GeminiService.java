package org.example.fashion_web.backend.services.impl;

import org.example.fashion_web.backend.configurations.GeminiConfig;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.ChatbotRepository;
import org.example.fashion_web.backend.repositories.UserChatbotRepository;
import org.example.fashion_web.backend.services.UserService;

import org.example.fashion_web.backend.repositories.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiService {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserChatbotRepository userChatbotRepository;

    @Autowired
    private ChatbotRepository chatbotRepository;

    @Autowired
    private UserService userService;

    private final WebClient webClient = WebClient.builder().build();


    public String chatWithAI(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").trim();

        // Truy vấn sản phẩm liên quan đến câu hỏi
        List<Product> relatedProducts = productRepository.findByNameContaining(normalizedMessage);


        String productInfo = relatedProducts.isEmpty()
                ? "Em chưa tìm thấy sản phẩm chính xác, nhưng anh/chị có thể tham khảo thêm các mẫu tương tự không ạ?"
                : relatedProducts.stream()
                .map(p -> "Tên: " + p.getName() + ", Giá: " + p.getPrice() + " VNĐ, Mô tả: " + p.getDescription())
                .collect(Collectors.joining("\n"));

        // Tạo câu hỏi gửi đến AI cùng với dữ liệu từ database
        String prompt = "Người dùng hỏi: '" + message + "'.\n\nDữ liệu từ hệ thống:\n" + productInfo + "\n\nHãy phản hồi thông minh dựa trên thông tin có sẵn.";

        String url = GEMINI_API_URL + "?key=" + geminiConfig.getApiKey();

        // Tạo JSON yêu cầu
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Phản hồi từ Gemini API (raw): " + response);
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("candidates")) {
                return jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } else {
                return "API không trả về phản hồi hợp lệ!";
            }

        } catch (WebClientResponseException e) {
            System.err.println("Lỗi từ Gemini API: " + e.getResponseBodyAsString());
            return "Lỗi từ Gemini API: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
    public String getGeminiResponse(String message) {
        String response;
        if (message.toLowerCase().contains("sản phẩm")) {
            response = searchProducts(message);
        } else if (message.toLowerCase().contains("tồn kho")) {
            response = checkStock(message);
        } else {
            response = "Xin lỗi, tôi chưa hiểu câu hỏi. Bạn có thể hỏi về sản phẩm hoặc tồn kho.";
        }
        return response;
    }

    public String searchProducts(String message) {
        List<Product> products = productRepository.findByNameContaining(message);
        return products.isEmpty() ? "Không tìm thấy sản phẩm phù hợp." : products.stream().map(Product::getName).collect(Collectors.joining(", "));
    }

    public String checkStock(String message) {
        // Chuẩn hóa câu hỏi để lấy đúng tên sản phẩm
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").trim();

        // Kiểm tra sản phẩm trong database
        List<Product> relatedProducts = productRepository.findAll()
                .stream()
                .filter(p -> normalizedMessage.toLowerCase().contains(p.getName().toLowerCase()))
                .collect(Collectors.toList());

        if (relatedProducts.isEmpty()) {
            return "Dạ, hiện tại em chưa tìm thấy sản phẩm '" + normalizedMessage + "' trong hệ thống. Anh/chị có muốn xem mẫu tương tự không ạ?";
        }

        // Định dạng giá tiền
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");

        // Tạo phản hồi với số lượng sản phẩm
        return relatedProducts.stream()
                .map(p -> "Dạ, shop hiện còn " + p.getStockQuantity() + " chiếc '" + p.getName() +
                        "' với giá " + (p.getPrice() != null ? formatter.format(p.getPrice()) : "Chưa có giá") + " ạ.")
                .collect(Collectors.joining("\n"));
    }

    // Lưu đoạn hội thoại vào database
    public void saveConversation(Long userId,String interactionLog) {
        // Tìm user trong database
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        // Tìm chatbot trong database
        Chatbot chatbot = chatbotRepository.findByUserId(userId);
        UserChatbotId userChatbotId = new UserChatbotId(chatbot.getId(), user.getId());

        // Tạo mới một đối tượng UserChatbot
        UserChatbot userChatbot = new UserChatbot();
        userChatbot.setId(userChatbotId);
        userChatbot.setUser(user);
        userChatbot.setChatbot(chatbot); // Đảm bảo chatbot luôn có giá trị

        userChatbot.setInteractionLog(interactionLog);
        userChatbot.setLastInteractionAt(LocalDateTime.now()); // Lưu thời gian hội thoại

        // Lưu vào database
        userChatbotRepository.save(userChatbot);
        System.out.println("Đã lưu đoạn hội thoại vào database.");
    }


//    public String getConversationHistory(Long userId) {
//        List<UserChatbot> history = userChatbotRepository.findTop5ByUserIdOrderByLastInteractionAtDesc(userId);
//        return history.stream().map(UserChatbot::getInteractionLog).collect(Collectors.joining("\n"));
//    }

      // Gợi ý theo người dùng
//    public String analyzeUserData(Long userId) {
//        List<Product> purchasedProducts = orderRepository.findPurchasedProductsByUserId(userId);
//        String dataSummary = purchasedProducts.stream().map(Product::getName).collect(Collectors.joining(", "));
//
//        String prompt = "Người dùng đã mua: " + dataSummary + ". Hãy gợi ý sản phẩm phù hợp.";
//        return chatWithAI(prompt);
//    }



}
