package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;

import java.math.BigDecimal;
import java.util.List;


public interface CartItemService {
    // Thêm sản phẩm vào giỏ hàng
    CartItems addToCart(User user, Long productId, int quantity);

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    CartItems updateCartItemQuantity(Long cartItemId, int newQuantity);

    // Xóa một sản phẩm khỏi giỏ hàng
    void removeCartItem(Integer cartItemId);

    //Lấy danh sách các cart item theo cart id
    List<CartItems> getCartItemByCartId(Integer cartId);

    BigDecimal getTotalPrice(List<CartItems> cartItems);

    CartItems save(CartItems item);
}
