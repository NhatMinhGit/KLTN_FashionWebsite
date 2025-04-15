package org.example.fashion_web.frontend.controller;

import lombok.Getter;
import org.bouncycastle.math.raw.Mod;
import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.OrderItem;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.services.OrderService;
import org.example.fashion_web.backend.services.OrderItemService;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StatisticController {
    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;

    @GetMapping("/admin/statistic")
    public String statisticalBoard(Model model, Principal principal) {
        // Xử lý thông tin người dùng nếu có
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            }
        }
        return "statistic/statistic";
    }

    @GetMapping("/admin/statistic/revenue/yearly")
    public ResponseEntity<?> getRevenueByYear(@RequestParam int year) {
        List<String> labels = Arrays.asList("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12");
        List<BigDecimal> data = orderService.getRevenueByYear(year);

        if (data == null || data.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Không có dữ liệu cho năm " + year);
        }

        return ResponseEntity.ok(Map.of(
                "labels", labels,
                "data", data,
                "label", "Doanh thu từng tháng năm " + year
        ));
    }

    @GetMapping("/admin/statistic/revenue/year")
    public ResponseEntity<?> getRevenueByYears() {
        Map<Integer, BigDecimal> yearRevenue = orderService.getRevenueByYears();
        List<String> labels = yearRevenue.keySet().stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<BigDecimal> data = new ArrayList<>(yearRevenue.values());

        return ResponseEntity.ok(Map.of(
                "labels", labels,
                "data", data,
                "label", "Doanh thu các năm"
        ));
    }

    @GetMapping("/admin/statistic/top-products")
    public ResponseEntity<?> getTopProducts(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> result = orderItemService.getTop10ProductsByRevenue(monthStart, monthEnd);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/statistic/top-categories")
    public ResponseEntity<?> getTopCategories(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<CategoryRevenueDto> result = orderItemService.getTop10CategoriesByRevenue(monthStart, monthEnd);
        return ResponseEntity.ok(result);
    }


//    @GetMapping("/admin/statistic/customers")
//    public ResponseEntity<?> getCustomerAnalysis() {
//        Map<String, Integer> customerData = customerService.getCustomerData(); // Thống kê theo độ tuổi, giới tính, khu vực
//        return ResponseEntity.ok(customerData);
//    }

}
