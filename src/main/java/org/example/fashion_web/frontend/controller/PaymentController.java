package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Controller
public class PaymentController {
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/user/order/payment")
    public String orderPaymentPage(HttpSession session, Model model) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        if (orderId == null) return "redirect:/user/order";

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) return "redirect:/user/order";
        model.addAttribute("deadlineTimestamp", LocalDateTime.now().plusHours(3).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        model.addAttribute("order", order.get());
        return "order/order-payment"; // chính là trang bạn đang thiết kế
    }

}
