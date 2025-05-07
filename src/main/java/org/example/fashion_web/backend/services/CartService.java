package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Cart;
import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    // Xóa toàn bộ giỏ hàng của user (dùng khi thanh toán hoặc hủy giỏ hàng)
    void clearCart(User user);

    // Lấy danh sách các mục trong giỏ hàng
    List<CartItems> getCartItems(Long id);

    // Tính tổng giá trị giỏ hàng
    BigDecimal getTotalPriceByCartId(Long id);

    Cart save(Cart cart);

    Integer getCountCart(Long userId);

}
