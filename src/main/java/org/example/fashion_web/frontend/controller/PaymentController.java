package org.example.fashion_web.frontend.controller;

import com.sendgrid.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.dto.EmailRequest;
import org.example.fashion_web.backend.dto.OrderDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.EmailService;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.SizeService;
import org.example.fashion_web.backend.services.VNPAYService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private SizeService sizeService;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

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
        User user = userRepository.findById(paymentInfo.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String bankCode = request.getParameter("bankCode");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        String vnpId = request.getParameter("vnp_TxnRef");

        if (paymentStatus == 1 && order.isPresent()) {
            // Cập nhật trạng thái đơn hàng
            Order o = order.get();
            o.setStatus(Order.OrderStatusType.PAID);
            o.setVnpId(vnpId);
            orderRepository.save(o);
            paymentInfo.setId(o.getId());
            for (CartItems item : cartItems) {
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
                                .filter(size -> size.getId().equals(item.getSize().getId())) // So sánh theo ID size
                                .findFirst();

                        sizeOpt.ifPresentOrElse(size -> {
                            if (size.getStockQuantity() >= item.getQuantity()) {
                                size.setStockQuantity(size.getStockQuantity()); // Giảm tồn kho size
                                sizeRepository.save(size); // Lưu lại size đã cập nhật
                            } else {
                                throw new RuntimeException("Not enough stock for size: " + size.getSizeName());
                            }
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

            // Gửi email
            try {
                String htmlContent = emailService.buildOrderConfirmationEmail(user, paymentInfo, cartItems);
                EmailRequest emailRequest = new EmailRequest(user.getEmail(), "Xác nhận đơn hàng #" + o.getId(), htmlContent);
                Response response = emailService.sendEmail(emailRequest, user.getEmail());

                if (response.getStatusCode() != 200 && response.getStatusCode() != 202) {
                    System.out.println("❌ Gửi email thất bại: " + response.getStatusCode());
                }
                session.removeAttribute("paymentOrderId");
                session.removeAttribute("cartItems");
                session.removeAttribute("paymentInfo");
            } catch (Exception e) {
                System.out.println("❌ Lỗi khi gửi email: " + e.getMessage());
            }

            return "/order/ordersuccess";
        } else  {
            // Cập nhật trạng thái đơn hàng
            Order o = order.get();
            o.setStatus(Order.OrderStatusType.PAYING);
            o.setVnpId(vnpId);

            orderRepository.save(o); 
            return "redirect:/user/order/payment-timer";
        }

    }

    @GetMapping("/user/order/payment-timer")
    public String paymentTimerPage(HttpSession session, Model model) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        if (orderId == null) {
            return "redirect:/user/order"; // Không có orderId -> về lại trang đặt hàng
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/user/order";
        }

        Order order = optionalOrder.get();

        // Tính số giây còn lại
        LocalDateTime now = LocalDateTime.now();
        long remainingSeconds = java.time.Duration.between(now, order.getExpireDate()).getSeconds();

        if (remainingSeconds <= 0) {
            return "redirect:/user/order"; // Hết hạn -> về lại
        }

        model.addAttribute("remainingSeconds", remainingSeconds);
        return "order/order-payment-timer"; // trỏ tới file payment-timer.html
    }

    @GetMapping("/user/order/continue-payment")
    public String continuePayment(HttpSession session, HttpServletRequest request) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        if (orderId == null) {
            return "redirect:/user/order";
        }
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return "redirect:" + vnPayService.createOrder(session, request, orderId, baseUrl);
    }

    @GetMapping("/user/order/cancel-payment")
    public String cancelPayment(HttpSession session) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        if (orderId != null) {
            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setStatus(Order.OrderStatusType.CANCELLED);
                orderRepository.save(order); // thêm dòng này để lưu vào DB
            }
            session.removeAttribute("paymentOrderId");
            session.removeAttribute("cartItems");
            session.removeAttribute("paymentInfo");
        }
        return "redirect:/user";
    }


}