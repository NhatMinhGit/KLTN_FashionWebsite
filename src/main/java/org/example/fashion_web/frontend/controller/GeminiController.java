package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.services.servicesimpl.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/user")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String message) {
        String response = geminiService.getGeminiResponse(message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chatbot")
    public ModelAndView chatbotPage() {
        return new ModelAndView("chat-window");
    }

}
