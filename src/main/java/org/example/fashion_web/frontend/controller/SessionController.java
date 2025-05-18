package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

    // Endpoint để client gọi giữ session sống
    @PostMapping("/keep-session-alive")
    public String keepSessionAlive(HttpSession session) {
        // Chỉ cần truy cập session để nó được giữ sống (Servlet container tự reset timeout)
        session.getId();  // hoặc session.invalidate() nếu muốn hủy session

        // Bạn có thể trả về thông báo đơn giản
        return "Session is alive. Session ID: " + session.getId();
    }
}
