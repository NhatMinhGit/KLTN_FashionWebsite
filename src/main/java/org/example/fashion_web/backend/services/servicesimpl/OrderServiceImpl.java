package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
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



}
