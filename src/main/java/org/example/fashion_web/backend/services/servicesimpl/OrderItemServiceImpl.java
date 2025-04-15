package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.OrderItem;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.services.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }
    @Override
    public List<ProductRevenueDto> getTop10ProductsByRevenue(LocalDate monthStart, LocalDate monthEnd) {
        List<ProductRevenueDto> result = orderItemRepository.findTopProductsByRevenue(monthStart, monthEnd);

        // Giới hạn kết quả chỉ lấy 10 sản phẩm có doanh thu cao nhất
        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    @Override
    public List<CategoryRevenueDto> getTop10CategoriesByRevenue(LocalDate monthStart, LocalDate monthEnd) {
        List<CategoryRevenueDto> result = orderItemRepository.findTopCategoriesByRevenue(monthStart, monthEnd);

        // Giới hạn kết quả chỉ lấy 10 danh mục có doanh thu cao nhất
        return result.size() > 10 ? result.subList(0, 10) : result;
    }
}
