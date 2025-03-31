package org.example.fashion_web.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserChatbotId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "chatbot_id", nullable = false)
    private Long chatbotId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserChatbotId that = (UserChatbotId) o;
        return Objects.equals(chatbotId, that.chatbotId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatbotId, userId);
    }
}
