package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.OrderItem;
import org.example.fashion_web.backend.repositories.CategoryRepository;
import org.example.fashion_web.backend.repositories.DiscountRepository;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.services.OrderItemService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;
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
        return result.size() > 3 ? result.subList(0, 3) : result;
    }

    @Override
    public List<ProductRevenueDto> getTopLow10ProductsByRevenue(LocalDate monthStart, LocalDate monthEnd) {
        List<ProductRevenueDto> result = orderItemRepository.findTopProductsByLowestRevenue(monthStart, monthEnd);

        // Giới hạn kết quả chỉ lấy 10 sản phẩm có doanh thu cao nhất
        return result.size() > 10 ? result.subList(0, 10) : result;
    }


}
