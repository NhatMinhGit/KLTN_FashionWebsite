package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.example.fashion_web.backend.dto.SimpleOrderDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
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

    public int getOrdersThisMonth() {
        return orderRepository.countOrdersThisMonth();
    }

    @Override
    @Transactional
    public List<Order> getAllOrders() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Lấy danh sách đơn hàng đang PAYING
        List<Order> payingOrders = orderRepository.findByStatus(Order.OrderStatusType.PAYING);

        // 2. Tách đơn đã hết hạn & đơn còn hiệu lực
        List<Order> expiredOrders = payingOrders.stream()
                .filter(order -> order.getExpireDate() != null && order.getExpireDate().isBefore(now))
                .collect(Collectors.toList());

        List<Order> validOrders = payingOrders.stream()
                .filter(order -> order.getExpireDate() != null && order.getExpireDate().isAfter(now))
                .collect(Collectors.toList());

        // 3. Cập nhật trạng thái đơn hết hạn => CANCELLED
        for (Order order : expiredOrders) {
            order.setStatus(Order.OrderStatusType.CANCELLED);
        }

        // 4. Lưu tất cả đơn đã bị thay đổi trạng thái
        orderRepository.saveAll(expiredOrders);

        return orderRepository.findAll(); // các đơn PAYING còn hiệu lực
    }

    @Override
    public boolean hasPayingOrder(User user) {
        return orderRepository.existsByUserAndStatus(user, Order.OrderStatusType.PAYING);
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



}
