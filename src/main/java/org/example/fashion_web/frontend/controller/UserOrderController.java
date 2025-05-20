package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.OrderItemDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.*;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.example.fashion_web.backend.utils.CurrencyFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserOrderController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CurrencyFormatter currencyFormatter;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FeedBackService feedBackService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SizeService sizeService;

    @Autowired
    private SizeRepository sizeRepository;

    @GetMapping("user/user-order")
    public String userOrderIndex(Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetail) {
        List<Order> orders = orderService.getAllOrders(userDetail.getUser().getId());

        model.addAttribute("ordersAll", orders);
        model.addAttribute("ordersPaid", filterByStatus(orders, Order.OrderStatusType.PAID));
        model.addAttribute("ordersPending", filterByStatus(orders, Order.OrderStatusType.PENDING));
        model.addAttribute("ordersPaying", filterByStatus(orders, Order.OrderStatusType.PAYING));
        model.addAttribute("ordersCompleted", filterByStatus(orders, Order.OrderStatusType.COMPLETED));
        model.addAttribute("ordersShipped", filterByStatus(orders, Order.OrderStatusType.SHIPPED));
        model.addAttribute("ordersCancelled", filterByStatus(orders, Order.OrderStatusType.CANCELLED));

        model.addAttribute("currencyFormatter", currencyFormatter);
        model.addAttribute("currentPage", "orders");
        // Tính thời gian còn lại (seconds) cho mỗi đơn hàng trạng thái PAYING
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Long> orderRemainingSeconds = new HashMap<>();
        for (Order order : filterByStatus(orders, Order.OrderStatusType.PAYING)) {
            if (order.getExpireDate() != null && order.getExpireDate().isAfter(now)) {
                long secondsLeft = java.time.Duration.between(now, order.getExpireDate()).getSeconds();
                orderRemainingSeconds.put(order.getId(), secondsLeft);
            } else {
                orderRemainingSeconds.put(order.getId(), 0L);
            }
        }
        model.addAttribute("orderRemainingSeconds", orderRemainingSeconds);

        return "user-order/user-order";
    }



    @GetMapping("user/user-order/{orderId}/items")
    @ResponseBody
    public List<OrderItemDto> getOrderItems(@PathVariable Long orderId,
                                            @AuthenticationPrincipal CustomUserDetails userDetail) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(orderId);
        Long userId = userDetail.getUser().getId(); // Lấy ID người dùng hiện tại

        return items.stream().map(item -> {
            String productName = item.getProduct().getName();
            Optional<Image> imageOpt = imageRepository.findFirstByProductVariant_Id(item.getProduct().getId());
            String imageUrl = imageOpt.map(Image::getImageUri).orElse("/img/no-image.png");
            String priceFormatted = currencyFormatter.formatVND(item.getPricePerUnit());
            Long productId = item.getProduct().getId();
            // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
            boolean hasReviewed = feedBackService.hasUserReviewedProduct(userId, productId);
            return new OrderItemDto(productName, imageUrl, item.getQuantity(), priceFormatted, productId, hasReviewed);
        }).toList();
    }

    @PostMapping("/user/user-order/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @AuthenticationPrincipal CustomUserDetails userDetail) {
        Order order = orderRepository.findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng.");
        }

        if (!order.getUser().getId().equals(userDetail.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền hủy đơn hàng này.");
        }

        if (order.getStatus() != Order.OrderStatusType.PENDING &&
                order.getStatus() != Order.OrderStatusType.PAYING &&
                    order.getStatus() != Order.OrderStatusType.PAID) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đơn hàng không thể hủy ở trạng thái hiện tại.");
        }

        order.setStatus(Order.OrderStatusType.CANCELLED);
        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);
        getProductBack(orderItems);
        orderRepository.save(order);

        return ResponseEntity.ok("Đơn hàng đã được hủy thành công.");
    }

    private List<Order> filterByStatus(List<Order> orders, Order.OrderStatusType status) {
        if (orders == null) return Collections.emptyList();
        return orders.stream()
                .filter(o -> o.getStatus().equals(status))
                .collect(Collectors.toList());
    }


    public void getProductBack(List<OrderItem> orderItems) {
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

    @PostMapping("/user/user-order/{orderId}/update-deadline")
    @ResponseBody
    public ResponseEntity<?> updateOrderDeadline(@PathVariable Long orderId,
                                                 @RequestBody Map<String, String> body,
                                                 @AuthenticationPrincipal CustomUserDetails userDetail) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null || !order.getUser().getId().equals(userDetail.getUser().getId())) {
            return ResponseEntity.status(403).body("Không có quyền.");
        }

        try {
            long deadlineMillis = Long.parseLong(body.get("deadline"));
            // Convert về thời gian hiện tại nếu muốn lưu dạng LocalDateTime
            LocalDateTime deadlineTime = LocalDateTime.ofInstant(
                    new Date(deadlineMillis).toInstant(), ZoneId.systemDefault()
            );

            // Gợi ý: thêm trường mới order.setDeadline(deadlineTime); nếu bạn lưu deadline riêng
            // Nếu bạn không có trường deadline, có thể skip việc lưu.

            System.out.println("Cập nhật deadline client gửi về: " + deadlineTime);
            // orderRepository.save(order); // nếu có field cần lưu

            return ResponseEntity.ok("Cập nhật deadline thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi dữ liệu deadline");
        }
    }


}
