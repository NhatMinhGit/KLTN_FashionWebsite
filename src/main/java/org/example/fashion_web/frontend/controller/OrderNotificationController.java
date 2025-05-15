package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.SimpleOrderDto;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class OrderNotificationController {

    @MessageMapping("/orders")
    @SendTo("/topic/orders")
    public List<SimpleOrderDto> handleOrders(List<SimpleOrderDto> orders) {
        return orders;
    }
}
