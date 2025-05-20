package org.example.fashion_web.backend.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.example.fashion_web.backend.dto.EmailRequest;
import org.example.fashion_web.backend.dto.OrderDto;
import org.example.fashion_web.backend.models.CartItems;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.OrderItem;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.OrderRepository;
import org.example.fashion_web.backend.utils.CurrencyFormatter;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class EmailService {

    @Autowired
    private SendGrid sendGrid;

    @Autowired
    private Email fromEmail;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private CurrencyFormatter currencyFormatter;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private SessionFactory sessionFactory;


    public Response sendEmail(EmailRequest emailRequest, String toEmail) {

        Content content = new Content("text/html", emailRequest.getBody());

        Mail mail = new Mail(fromEmail, emailRequest.getSubject(), new Email(emailRequest.getTo()), content);
        mail.setReplyTo(new Email(toEmail));

        Request request = new Request();
        Response response = null;

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            response = this.sendGrid.api(request);

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return response;
    }

    public String buildOrderConfirmationEmail(User user, Order paymentInfo, List<OrderItem> orderItems) {
        Context context = new Context();
        context.setVariable("userName", user.getName());
        context.setVariable("orderId", paymentInfo.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        context.setVariable("orderDate", paymentInfo.getCreatedAt().format(formatter));

        context.setVariable("shippingAddress", paymentInfo.getShippingAddress());
        context.setVariable("paymentMethod", paymentInfo.getPaymentMethod());
        context.setVariable("totalAmount", CurrencyFormatter.formatVND(paymentInfo.getTotalPrice()));

        StringBuilder productHtml = new StringBuilder();
        for (OrderItem item : orderItems) {
            BigDecimal total = item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity()));
            productHtml.append("<div class='item'>")
                    .append("- ").append(item.getProduct().getName())
                    .append(" (Size: ").append(item.getSize().getSizeName())
                    .append(", Màu: ").append(item.getVariant().getColor())
                    .append("): x").append(item.getQuantity())
                    .append(" = ").append(CurrencyFormatter.formatVND(total))
                    .append("</div>");
        }

        context.setVariable("productList", productHtml.toString());

        return templateEngine.process("email/email-order-confirmation", context);
    }

}
