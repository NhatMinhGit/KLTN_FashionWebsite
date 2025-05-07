package org.example.fashion_web.frontend.controller;

import com.sendgrid.Response;
import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.dto.EmailRequest;
import org.example.fashion_web.backend.dto.OrderDto;
import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.EmailService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sendemail")
    public ResponseEntity<String> sendEmail(HttpSession session, Model model, @AuthenticationPrincipal CustomUserDetails userDetail) {
        try {
            OrderDto paymentInfo = (OrderDto) session.getAttribute("paymentInfo");
            List<CartItems> cartItems = (List<CartItems>) session.getAttribute("cartItems");

            User user = userRepository.findById(userDetail.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String htmlContent = emailService.buildOrderConfirmationEmail(user, paymentInfo, cartItems);

            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(user.getEmail());
            emailRequest.setSubject("Xác nhận đơn hàng #" + paymentInfo.getId());
            emailRequest.setBody(htmlContent);

            Response response = emailService.sendEmail(emailRequest, user.getEmail());
            if (response.getStatusCode() == 200 || response.getStatusCode() == 202) {
                return new ResponseEntity<>("send successfully", HttpStatus.OK);
            }

            return new ResponseEntity<>("failed to send", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Order processing failed: " + e.getMessage());
            System.out.println(e.getMessage());
            return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
