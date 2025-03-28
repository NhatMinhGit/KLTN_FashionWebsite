package org.example.fashion_web.backend.services.impl;

import org.example.fashion_web.backend.models.OrderItem;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.services.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
}
