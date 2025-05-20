package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.OrderService;
import org.example.fashion_web.backend.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.example.fashion_web.backend.dto.SimpleOrderDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SizeService sizeService;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public Order findOrderById(Long id) {
        if (id == null) {
            return null;
        }
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Order save(Order Order) {
        return orderRepository.save(Order);
    }

    @Override
    public List<BigDecimal> getRevenueByYear(int year) {
        List<BigDecimal> revenues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            revenues.add(orderRepository.getMonthlyRevenue(year, i));
        }
        return revenues;
    }

    @Override
    public Map<Integer, BigDecimal> getRevenueByYears() {
        List<Object[]> rawData = orderRepository.getRevenueByYears();
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : rawData) {
            result.put((Integer) row[0], (BigDecimal) row[1]);
        }
        return result;
    }

    @Override
    public int getTotalOrdersCount() {
        return orderRepository.findAll().size();
    }

    @Override
    public List<OrderStatusDto> getOrderStatusData(int year, int month) {
        List<Object[]> results = orderRepository.findPaymentStatusCountByYearAndMonth(year, month);

        List<OrderStatusDto> orderStatusDtos = new ArrayList<>();
        for (Object[] result : results) {
            String paymentStatus = String.valueOf(result[0]); // đảm bảo lấy ra dưới dạng String

            // Xử lý đổi tên status
            String displayStatus;
            if ("0".equals(paymentStatus)) {
                displayStatus = "Chưa thanh toán";
            } else if ("1".equals(paymentStatus)) {
                displayStatus = "Đã thanh toán";
            } else {
                displayStatus = "Không rõ";
            }

            Number countNumber = (Number) result[1];
            int count = countNumber.intValue();

            orderStatusDtos.add(new OrderStatusDto(displayStatus, count));
        }
        return orderStatusDtos;
    }


    public int getOrdersToday() {
        return orderRepository.countOrdersToday();
    }

    @Override
    public List<Order> findOrdersByUserAndStatusIn(User user, List<Order.OrderStatusType> statuses) {
        return orderRepository.findByUserAndStatusIn(user,statuses);
    }

    public int getOrdersThisMonth() {
        return orderRepository.countOrdersThisMonth();
    }

    @Override
    @Transactional
    public List<Order> getAllOrders(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findById(userId).get();
        // 1. Lấy danh sách đơn hàng đang PAYING
        List<Order> payingOrders = orderRepository.findByUserAndStatusIn(user, Collections.singletonList(Order.OrderStatusType.PAYING));

        // 2. Tách đơn đã hết hạn & đơn còn hiệu lực
        List<Order> expiredOrders = payingOrders.stream()
                .filter(order -> order.getExpireDate() != null && order.getExpireDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        List<Order> validOrders = payingOrders.stream()
                .filter(order -> order.getExpireDate() != null && order.getExpireDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        // 3. Cập nhật trạng thái đơn hết hạn => CANCELLED
        for (Order order : expiredOrders) {
            order.setStatus(Order.OrderStatusType.CANCELLED);
            List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(order.getId());
            for (OrderItem item : orderItems) {
                Optional<Product> productOpt = productRepository.findById(item.getProduct().getId());

                productOpt.ifPresentOrElse(product -> {
                    // Tìm variant tương ứng với sản phẩm
                    Optional<ProductVariant> variantOpt = product.getVariants().stream()
                            .filter(variant -> variant.getId().equals(item.getVariant().getId())) // so sánh theo ID variant
                            .findFirst();

                    variantOpt.ifPresentOrElse(variant -> {
                        // Thay vì lấy từ variant.getSizes(), gọi sizeService
                        List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                        Optional<Size> sizeOpt = sizes.stream()
                                .filter(size -> size.getId().equals(item.getSize().getId()))// So sánh theo ID size
                                .findFirst();

                        sizeOpt.ifPresentOrElse(size -> {
                            size.setStockQuantity(size.getStockQuantity() + item.getQuantity()); // Giảm tồn kho size
                            size.setStockQuantity(size.getStockQuantity());
                            sizeRepository.save(size); // Lưu lại size đã cập nhật
                        }, () -> {
                            throw new RuntimeException("Size not found for variant: " + variant.getColor());
                        });

                    }, () -> {
                        throw new RuntimeException("Variant not found for product: " + product.getName());
                    });

                }, () -> {
                    throw new RuntimeException("Product not found with ID: " + item.getProduct().getId());
                });
            }
        }

        // 4. Lưu tất cả đơn đã bị thay đổi trạng thái
        orderRepository.saveAll(expiredOrders);

        return orderRepository.findByUser_IdOrderByIdDesc(userId);
    }

    @Override
    public boolean hasPayingOrder(User user) {
        return orderRepository.existsByUserAndStatus(user, Order.OrderStatusType.PAYING);
    }

    @Override
    public int getTotalOrdersNotCancelled() {
        List<Order.OrderStatusType> list = new ArrayList<>();
        list.add(Order.OrderStatusType.PAYING);
        list.add(Order.OrderStatusType.PAID);
        list.add(Order.OrderStatusType.COMPLETED);
        list.add(Order.OrderStatusType.SHIPPED);
        list.add(Order.OrderStatusType.PENDING);
        return orderRepository.findByStatusIn(list).size();
    }

    @Override
    public void notifyNewOrders(List<Order> orders) {
        List<SimpleOrderDto> dtoList = orders.stream()
                .map(order -> new SimpleOrderDto(order.getId(),
                        order.getUser().getName(),
                        order.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))))
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend("/topic/orders", dtoList);
    }

    @Override
    public BigDecimal getTotalRevenueCompletedOrders() {
        List<Order> completedOrders = orderRepository.findByStatus(Order.OrderStatusType.COMPLETED);
        return completedOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Order> searchOrders(String keyword, Order.OrderStatusType status) {
        if ((keyword == null || keyword.trim().isEmpty()) && status == null) {
            return orderRepository.findAll();
        }
        return orderRepository.searchOrders(keyword, status);
    }

    @Override
    public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

}
