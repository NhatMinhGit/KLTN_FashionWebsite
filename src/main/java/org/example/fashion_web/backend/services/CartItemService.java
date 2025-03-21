package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;


public interface CartItemService {
    // Thêm sản phẩm vào giỏ hàng
    CartItems addToCart(User user, Long productId, int quantity);

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    CartItems updateCartItemQuantity(Long cartItemId, int newQuantity);

    // Xóa một sản phẩm khỏi giỏ hàng
    void removeCartItem(Long cartItemId);

    CartItems save(CartItems item);
}
