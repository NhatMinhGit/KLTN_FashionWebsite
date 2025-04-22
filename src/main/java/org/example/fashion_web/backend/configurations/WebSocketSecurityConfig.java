//package org.example.fashion_web.backend.configurations;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
//import org.springframework.security.config.annotation.web.messaging.WebSocketMessageBrokerSecurityConfig;
//
//@Configuration
//public class WebSocketSecurityConfig extends WebSocketMessageBrokerSecurityConfig {
//
//    @Override
//    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//        // Cấu hình quyền truy cập cho các yêu cầu WebSocket
//        messages
//                .simpDestMatchers("/app/**").authenticated()  // yêu cầu người dùng đã xác thực khi gửi tin nhắn
//                .simpSubscribeDestMatchers("/user/**", "/user/queue/**").authenticated()  // yêu cầu xác thực khi đăng ký nhận tin nhắn
//                .anyMessage().denyAll();  // Từ chối các yêu cầu không được xác thực
//    }
//
//    // Tùy chọn: cho phép sử dụng CSRF với WebSocket (nếu cần thiết)
//    @Override
//    protected boolean sameOriginDisabled() {
//        return true;
//    }
//}
