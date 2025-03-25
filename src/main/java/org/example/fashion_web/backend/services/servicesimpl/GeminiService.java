package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.configurations.GeminiConfig;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.LinkedHashMap;
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

    private final WebClient webClient = WebClient.builder().build();



    public String getGeminiResponse(String message) {
        // Truy vấn sản phẩm liên quan đến câu hỏi
        List<Product> relatedProducts = productRepository.findByNameContaining(message);

        // Chuyển danh sách sản phẩm thành đoạn văn bản mô tả
        String productInfo = relatedProducts.isEmpty()
                ? "Không tìm thấy sản phẩm liên quan."
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


}
