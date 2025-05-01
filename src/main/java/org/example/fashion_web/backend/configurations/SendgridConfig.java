package org.example.fashion_web.backend.configurations;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendgridConfig {
    @Value("${sendgrid.api.key}")
    private String sendgridKey;

    @Value("${from.email.default}")
    private String fromEmail;

    @Bean
    public SendGrid getSendGrid() {
        return new SendGrid(sendgridKey);
    }

    @Bean
    public Email getfromEmail() {
        return new Email(fromEmail);
    }
}
