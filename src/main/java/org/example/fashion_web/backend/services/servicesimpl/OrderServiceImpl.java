package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Order;
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

}
