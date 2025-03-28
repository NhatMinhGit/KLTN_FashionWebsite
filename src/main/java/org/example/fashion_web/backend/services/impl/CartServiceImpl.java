package org.example.fashion_web.backend.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.fashion_web.backend.models.Cart;
import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.CartRepository;
import org.example.fashion_web.backend.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public void clearCart(User user) {

    }

    @Override
    public List<CartItems> getCartItems(Long id) {
        return cartRepository.getCartItemsByCartId(id);
    }

    @Override
    public BigDecimal getTotalPriceByCartId(Long id) {

        return cartRepository.getTotalPriceByCartId(id);
    }

    @Transactional // Bảo đảm dữ liệu được lưu ngay lập tức
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Integer getCountCart(Long userId) {
        Integer countByUserId = cartRepository.countByUserId(userId);
        return countByUserId ;
    }
}
