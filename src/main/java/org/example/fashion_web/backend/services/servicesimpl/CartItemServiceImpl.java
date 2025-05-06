package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.CartItemRepository;
import org.example.fashion_web.backend.services.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartItems addToCart(User user, Long productId, int quantity) {
        return null;
    }

    @Override
    public CartItems updateCartItemQuantity(Integer cartItemId, int newQuantity) {
        CartItems item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem with id " + cartItemId + " not found"));

        item.setQuantity(newQuantity);
        return cartItemRepository.save(item);
    }


    @Override
    public void removeCartItem(Integer cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new RuntimeException("Không tìm thấy sản phẩm để xóa với ID: " + cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public List<CartItems> getCartItemByCartId(Integer cartId) {
        List<CartItems> list = cartItemRepository.findByCartItemId(cartId);
        if (list == null) {
            throw new RuntimeException("Giỏ hàng chưa có sản phẩm!");
        }
        return list;
    }

    @Override
    public BigDecimal getTotalPrice(List<CartItems> cartItems) {
        BigDecimal totalOrderPrice = BigDecimal.valueOf(0);
        BigDecimal quantityOfEachItem;
        // Nhóm danh sách ảnh theo productId
        Map<Long, List<String>> productImages = new HashMap<>();
        for (CartItems item : cartItems) {
            quantityOfEachItem = new BigDecimal(item.getQuantity());
            totalOrderPrice = totalOrderPrice.add(item.getPricePerUnit().multiply(quantityOfEachItem));
        }
        return totalOrderPrice;
    }

    @Override
    public CartItems save(CartItems item) {
        return cartItemRepository.save(item);
    }

    @Override
    public Optional<CartItems> findById(Integer id) {
        return cartItemRepository.findById(id);
    }
}
