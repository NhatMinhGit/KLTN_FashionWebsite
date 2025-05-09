package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.dto.RevenueStatisticDto;
import org.example.fashion_web.backend.services.CartItemService;
import org.example.fashion_web.backend.services.OrderItemService;
import org.example.fashion_web.backend.services.OrderService;
import org.example.fashion_web.backend.services.SizeService;
import org.example.fashion_web.backend.services.servicesimpl.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StatisticController {
    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private ExcelExportService excelExportService;
    @Autowired
    private SizeService sizeService;

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
        RevenueStatisticDto dto = new RevenueStatisticDto(labels, data, "Doanh thu từng tháng năm " + year);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/statistic/revenue/year")
    public ResponseEntity<?> getRevenueByYears() {
        Map<Integer, BigDecimal> yearRevenue = orderService.getRevenueByYears();
        List<String> labels = yearRevenue.keySet().stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<BigDecimal> data = new ArrayList<>(yearRevenue.values());

        RevenueStatisticDto dto = new RevenueStatisticDto(labels, data, "Doanh thu các năm");
        return ResponseEntity.ok(dto);
    }


    @GetMapping("/admin/statistic/top-products")
    public ResponseEntity<?> getTopProducts(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> result = orderItemService.getTop10ProductsByRevenue(monthStart, monthEnd);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/admin/statistic/top-low-products")
    public ResponseEntity<?> getTopLowProducts(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> result = orderItemService.getTopLow10ProductsByRevenue(monthStart, monthEnd);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/statistic/top-categories")
    public ResponseEntity<?> getTopCategories(@RequestParam("year") int year, @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<CategoryRevenueDto> result = orderItemService.getTop10CategoriesByRevenue(monthStart, monthEnd);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/admin/statistic/order-status")
    public ResponseEntity<List<OrderStatusDto>> getOrderStatusChart(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        try {
            List<OrderStatusDto> data = orderService.getOrderStatusData(year, month);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

//    @GetMapping("/admin/statistic/customers")
//    public ResponseEntity<?> getCustomerAnalysis() {
//        Map<String, Integer> customerData = customerService.getCustomerData(); // Thống kê theo độ tuổi, giới tính, khu vực
//        return ResponseEntity.ok(customerData);
//    }

    @GetMapping("/admin/statistic/top-categories/excel")
    public ResponseEntity<?> exportTopCategoriesToExcel(@RequestParam("year") int year,
                                                        @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<CategoryRevenueDto> result = orderItemService.getTop10CategoriesByRevenue(monthStart, monthEnd);

        try {
            ByteArrayInputStream in = excelExportService.categoriesToExcel(result,monthStart,monthEnd);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=top_categories_" + year + "_" + month + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất Excel");
        }
    }

    @GetMapping("/admin/statistic/top-products/excel")
    public ResponseEntity<?> exportTopProductsToExcel(@RequestParam("year") int year,
                                                      @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> result = orderItemService.getTop10ProductsByRevenue(monthStart, monthEnd);

        try {
            ByteArrayInputStream in = excelExportService.productsToExcel(result,monthStart,monthEnd);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=top_products_" + year + "_" + month + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất Excel");
        }
    }
    @GetMapping("/admin/statistic/order-status/excel")
    public ResponseEntity<?> exportOrderStatusToExcel(@RequestParam("year") int year,
                                                      @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<OrderStatusDto> result = orderService.getOrderStatusData(year, month);

        try {
            ByteArrayInputStream in = excelExportService.orderStatusToExcel(result, monthStart, monthEnd);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=order_status_" + year + "_" + month + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất Excel");
        }
    }


    @GetMapping("/admin/statistic/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalStock", sizeService.getTotalStock());
        summary.put("stockValue", sizeService.getStockValue());
        summary.put("ordersToday", orderService.getOrdersToday());
        summary.put("ordersThisMonth", orderService.getOrdersThisMonth());

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/admin/statistic/top-low-products/excel")
    public ResponseEntity<?> exportTopLowProductsToExcel(@RequestParam("year") int year,
                                                      @RequestParam("month") int month) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> result = orderItemService.getTopLow10ProductsByRevenue(monthStart, monthEnd);

        try {
            ByteArrayInputStream in = excelExportService.productsToExcel(result,monthStart,monthEnd);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=top_low_products_" + year + "_" + month + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất Excel");
        }
    }

    @GetMapping("/admin/statistic/revenue/year/excel")
    public ResponseEntity<?> exportRevenueByYearsToExcel() {
        Map<Integer, BigDecimal> yearRevenue = orderService.getRevenueByYears();
        List<String> labels = yearRevenue.keySet().stream().map(String::valueOf).collect(Collectors.toList());
        List<BigDecimal> data = yearRevenue.values().stream().collect(Collectors.toList());

        try {
            ByteArrayInputStream in = excelExportService.revenueByYearToExcel(labels, data, "Doanh thu các năm");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=revenue_years.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất Excel");
        }
    }

    @GetMapping("/admin/statistic/revenue/yearly/excel")
    public ResponseEntity<?> exportRevenueByYearToExcel(@RequestParam int year) {
        List<String> labels = List.of("Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12");
        List<BigDecimal> data = orderService.getRevenueByYear(year);

        try {
            ByteArrayInputStream in = excelExportService.revenueByYearToExcel(labels, data, "Doanh thu từng tháng năm " + year);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=revenue_year_" + year + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xuất Excel");
        }
    }
}
