package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface OrderRepository extends JpaRepository<Order, Long> {
    Order save(Order order);
}
