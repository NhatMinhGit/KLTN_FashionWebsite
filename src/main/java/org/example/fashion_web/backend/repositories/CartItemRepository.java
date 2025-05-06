package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, Integer> {
    // Kiểm tra xem sản phẩm có tồn tại không
    boolean existsById(Integer id);

    // lấy danh sách các item trong giỏ hàng
    List<CartItems> findByCartItemId(Integer Long);

    Optional<CartItems> findById(Integer id);


}