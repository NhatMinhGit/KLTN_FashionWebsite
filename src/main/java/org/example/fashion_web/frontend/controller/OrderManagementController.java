package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.OrderItem;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserProfile;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.repositories.UserProfileRepository;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.OrderService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
@Controller
public class OrderManagementController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/admin/order")
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal) {

        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            model.addAttribute("user", userDetails);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> orderPage = orderRepository.findAll(pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "order/order-management-paging";
    }


    @RequestMapping("/admin/order/detail/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/order?error=notfound";
        }

        model.addAttribute("order", order);
        return "order/order-detail";
    }

    @PostMapping("/admin/order/approve/{id}")
    public String approveOrder(@PathVariable("id") Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null && (order.getStatus() == Order.OrderStatusType.PAID || order.getStatus() == Order.OrderStatusType.PENDING)) {
            order.setStatus(Order.OrderStatusType.SHIPPED);
            orderRepository.save(order);
        }

        return "redirect:/admin/order/detail/" + id;
    }

    /// xử lý xuất file pdf
    @GetMapping("/admin/order/export-invoice-pdf/{id}")
    public void exportPdf(HttpServletResponse response,
                            @PathVariable("id") Long id) throws Exception {
        Order order = orderRepository.findById(id).orElse(null);
        User user = userRepository.findById(order.getUser().getId()).orElse(null);
        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(id);

        UserProfile userProfile = userProfileRepository.findByUser_Id(user.getId());
        // Chuẩn bị dữ liệu cho template
        Context context = new Context();
        context.setVariable("customerName", user.getName());
        context.setVariable("purchaseDate",order.getOrderDate());
        context.setVariable("totalAmount", order.getTotalPrice());
        context.setVariable("phoneNumber", userProfile.getPhoneNumber());
        context.setVariable("shippingAddress", order.getShippingAddress());
        context.setVariable("items", orderItems);

        // Render HTML từ Thymeleaf
        String htmlContent = templateEngine.process("/pdf-template/invoice", context);

        // Gửi PDF về trình duyệt
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");

        try (OutputStream out = response.getOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            //font hỗ trợ tiếng Việt
            builder.useFont(new File("src/main/resources/fonts/DejaVuSans.ttf"), "MyFont");
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(out);
            builder.run();
        }
    }
}
