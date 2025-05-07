package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.List;
@Controller
public class OrderManagementController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserDetailsService userDetailsService;

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

}
