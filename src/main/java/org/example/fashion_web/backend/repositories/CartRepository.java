package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Cart;
import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    List<CartItems> getCartItemsByCartId(Long id);

    BigDecimal getTotalPriceByCartId(Long id);

    Integer countByUserId(Long userId);
}