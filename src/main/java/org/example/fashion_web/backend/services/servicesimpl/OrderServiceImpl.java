package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Override
    public Order save(Order Order) {
        return orderRepository.save(Order);
    }
}
