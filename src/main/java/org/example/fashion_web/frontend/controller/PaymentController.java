package org.example.fashion_web.frontend.controller;

import com.sendgrid.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.dto.EmailRequest;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private SizeService sizeService;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserVoucherAssignmentRepository userVoucherAssignmentRepository;


    @GetMapping("/user/order/submitOrder")
    public String rejectGetSubmitOrder() {
        return "redirect:/user/order?error=invalid-method";
    }

    public String processVNPayRedirect(HttpSession session, HttpServletRequest request) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return vnPayService.createOrder(request, orderId, baseUrl);
    }

    // Sau khi hoàn tất thanh toán, VNPAY sẽ chuyển hướng trình duyệt về URL này
    @GetMapping("/user/order/vnpay-payment-return")
    @Transactional(rollbackFor = Exception.class)
    public String paymentCompleted(HttpSession session, HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);
//        List<CartItems> cartItems = (List<CartItems>) session.getAttribute("cartItems"); / xử lý trên order item
//        OrderDto paymentInfo = (OrderDto) session.getAttribute("paymentInfo"); / xử lý trên order
//
        Long orderIdSession = (Long) session.getAttribute("paymentOrderId");//sẽ xử lý trên parameter cả vnpay

        //lấy order id từ tham số truyền
        String orderInfo = request.getParameter("vnp_OrderInfo"); // Ví dụ: "Thanh toan don hang #123"
        Long orderId = null;
        if (orderInfo != null && orderInfo.contains("#")) {
            try {
                orderId = Long.parseLong(orderInfo.substring(orderInfo.indexOf("#") + 1));
            } catch (NumberFormatException e) {
                throw new RuntimeException("OrderId không hợp lệ");
            }
        }
        if (orderId == null) {
           throw new RuntimeException("Không tìm thấy đơn hàng!");
        }

        Optional<Order> order = orderRepository.findById(orderId);
        User user = order.get().getUser();
        if (user == null) {
            throw new RuntimeException("Không tìm thấy user");
        }

        String bankCode = request.getParameter("bankCode");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        String vnpId = request.getParameter("vnp_TxnRef");

        if (paymentStatus == 1 && order != null && order.get().getStatus() != Order.OrderStatusType.PAID) {
            // Cập nhật trạng thái đơn hàng
            Order o = order.get();
            o.setStatus(Order.OrderStatusType.PAID);
            o.setVnpId(vnpId);
            orderRepository.save(o);
            List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);
            // Lưu voucher (nếu có)
            Payment payment = paymentRepository.findByOrderId(o.getId());
            // Gửi dữ liệu cho view Thymeleaf
            model.addAttribute("orderId", o.getId());  // Hiển thị mã đơn
            model.addAttribute("orderDate", o.getCreatedAt().toLocalDate()); // Ngày đặt
            model.addAttribute("totalAmount", order.get().getTotalPrice()); // định dạng nếu cần
            model.addAttribute("paymentMethod", payment.getPaymentMethod().toString());
            model.addAttribute("orderItems", orderItems);  // để hiển thị danh sách sản phẩm đã mua
            if (orderId.equals(orderIdSession)) {
                session.removeAttribute("paymentOrderId");
                session.removeAttribute("cartItems");
                session.removeAttribute("paymentInfo");
            }
            // Gửi email
            try {
                String htmlContent = emailService.buildOrderConfirmationEmail(user, o, orderItems);
                EmailRequest emailRequest = new EmailRequest(user.getEmail(), "Xác nhận đơn hàng #" + o.getId(), htmlContent);
                Response response = emailService.sendEmail(emailRequest, user.getEmail());

                if (response.getStatusCode() != 200 && response.getStatusCode() != 202) {
                    System.out.println("❌ Gửi email thất bại: " + response.getStatusCode());
                }

            } catch (Exception e) {
                System.out.println("❌ Lỗi khi gửi email: " + e.getMessage());
            }
            orderService.notifyNewOrders(List.of(o));
            return "order/ordersuccess";
        } else  {
            // Cập nhật trạng thái đơn hàng
            Order o = order.get();
            o.setStatus(Order.OrderStatusType.PAYING);
            o.setVnpId(vnpId);

            orderRepository.save(o); 
            return "redirect:/user/order/payment-timer";
        }

    }

    @GetMapping("/user/order/continue-payment")
    public String continuePayment(@RequestParam(value = "id", required = false) Long orderId,
                                  HttpSession session,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        Long orderIdSession = (Long) session.getAttribute("paymentOrderId");

        if (orderId == null && orderIdSession == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
            return "redirect:/user/user-order";
        }

        Long idToUse = (orderId != null) ? orderId : orderIdSession;
        Optional<Order> orderOptional = orderRepository.findById(idToUse);
        if (orderOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng không tồn tại.");
            return "redirect:/user/user-order";
        }

        Order order = orderOptional.get();

        if (order.getExpireDate() != null && order.getExpireDate().isBefore(LocalDateTime.now())) {
            order.setStatus(Order.OrderStatusType.CANCELLED);
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("errorMessage", "Đơn hàng #" + idToUse + " đã hết hạn và bị hủy.");
            return "redirect:/user/user-order";
        }

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return "redirect:" + vnPayService.createOrder(request, idToUse, baseUrl);
    }



    @GetMapping("/user/order/cancel-payment")
    public String cancelPayment(HttpSession session) {
        Long orderId = (Long) session.getAttribute("paymentOrderId");
        if (orderId != null) {
            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);
                order.setStatus(Order.OrderStatusType.CANCELLED);
                getProductBack(orderItems);
                orderRepository.save(order); // thêm dòng này để lưu vào DB
            }
            session.removeAttribute("paymentOrderId");
            session.removeAttribute("paymentInfo");
        }
        return "redirect:/user";
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


}