package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.OrderItemDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.ImageRepository;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.FeedBackService;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.OrderService;
import org.example.fashion_web.backend.services.VoucherService;
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

@Controller
public class UserOrderController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private CurrencyFormatter currencyFormatter;

    @Autowired
    private ImageService imageService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FeedBackService feedBackService;

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("user/user-order")
    public String userOrderIndex(Model model,
                                 @AuthenticationPrincipal CustomUserDetails userDetail) {
        List<Order> orders = orderRepository.findByUser_IdOrderByIdDesc(userDetail.getUser().getId());

        model.addAttribute("currentPage", "orders");
        model.addAttribute("orders", orders);
        model.addAttribute("currencyFormatter", currencyFormatter);

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
                order.getStatus() != Order.OrderStatusType.PAYING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Đơn hàng không thể hủy ở trạng thái hiện tại.");
        }

        order.setStatus(Order.OrderStatusType.CANCELLED);
        orderRepository.save(order);

        return ResponseEntity.ok("Đơn hàng đã được hủy thành công.");
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
