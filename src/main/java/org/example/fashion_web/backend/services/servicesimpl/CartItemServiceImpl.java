package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.CartItemRepository;
import org.example.fashion_web.backend.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartItems addToCart(User user, Long productId, int quantity) {
        return null;
    }

    @Override
    public CartItems updateCartItemQuantity(Long cartItemId, int newQuantity) {
        return null;
    }

    @Override
    public void removeCartItem(Long cartItemId) {

    }

    @Override
    public CartItems save(CartItems item) {
        return cartItemRepository.save(item);
    }
}
