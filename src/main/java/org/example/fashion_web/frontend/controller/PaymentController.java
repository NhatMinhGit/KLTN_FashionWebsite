package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.configurations.VNPAYService;
import org.example.fashion_web.backend.dto.OrderDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PaymentController {
    @Autowired
    private VNPAYService vnPayService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private  UserVoucherRepository userVoucherRepository;

    @Autowired
    private ImageService imageService;

    @GetMapping("/user/order/submitOrder")
    public String rejectGetSubmitOrder() {
        return "redirect:/user/order?error=invalid-method";
    }

    public String processVNPayRedirect(HttpSession session, HttpServletRequest request) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return vnPayService.createOrder(session, request, orderId, baseUrl);
    }
    // Sau khi hoàn tất thanh toán, VNPAY sẽ chuyển hướng trình duyệt về URL này
    @GetMapping("/user/order/vnpay-payment-return")
    public String paymentCompleted(HttpSession session, HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);
        List<CartItems> cartItems = (List<CartItems>) session.getAttribute("cartItems");
        OrderDto paymentInfo = (OrderDto) session.getAttribute("paymentInfo");
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        Optional<Order> order = orderRepository.findById(orderId);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String bankCode = request.getParameter("bankCode");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        String vnpId= request.getParameter("vnp_TxnRef");

        if (paymentStatus == 1 && order.isPresent()) {
            // Cập nhật trạng thái đơn hàng
            Order o = order.get();
            o.setStatus(Order.OrderStatusType.SHIPPED);
            o.setCreatedAt(LocalDateTime.now());
            o.setVnpId(vnpId);
            orderRepository.save(o);
            // Nhóm danh sách ảnh theo productId
            Map<Long, List<String>> productImages = new HashMap<>();
            for (CartItems item : cartItems) {
                List<Image> images = imageService.findImagesByProductId(item.getProduct().getId());
                List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
                productImages.put(item.getProduct().getId(), imageUrls);
            }
            // Lưu các mục trong giỏ hàng thành OrderItem
            for (CartItems item : cartItems) {
                Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (product != null) {
                    product.setStock_quantity(product.getStock_quantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }

            // Lưu thông tin thanh toán
            Payment payment = new Payment();
            payment.setOrder(o);
            payment.setPaymentMethod(Payment.PaymentMethodType.valueOf(paymentInfo.getPaymentMethod()));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus(String.valueOf(1));
            paymentRepository.save(payment);

            // Lưu voucher (nếu có)
            if (paymentInfo.getVoucherCode() != null && !paymentInfo.getVoucherCode().isEmpty()) {
                Voucher voucher = voucherRepository.findByVoucherCode(paymentInfo.getVoucherCode());
                if (voucher != null) {
                    voucher.setUsageLimit(voucher.getUsageLimit() - 1);
                    voucherRepository.save(voucher);

                    UserVoucher userVoucher = new UserVoucher();
                    userVoucher.setUser(o.getUser());
                    userVoucher.setOrder(o);
                    userVoucher.setVoucher(voucher);
                    userVoucher.setUsedDate(LocalDateTime.now());
                    userVoucherRepository.save(userVoucher);
                }
            }
            // Gửi dữ liệu cho view Thymeleaf
            model.addAttribute("orderId", o.getId());  // Hiển thị mã đơn
            model.addAttribute("orderDate", o.getCreatedAt().toLocalDate()); // Ngày đặt
            model.addAttribute("totalAmount", paymentInfo.getTotalPrice()); // định dạng nếu cần
            model.addAttribute("paymentMethod", payment.getPaymentMethod().toString());
            model.addAttribute("cartItems", cartItems);  // để hiển thị danh sách sản phẩm đã mua
            model.addAttribute("productImages", productImages);
            // Dọn session
            session.removeAttribute("cartItems");
            session.removeAttribute("paymentInfo");
            session.removeAttribute("paymentOrderId");

        }

        return paymentStatus == 1 ? "/order/ordersuccess" : "/user";
    }

}