package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.UserChatbot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChatbotRepository extends JpaRepository<UserChatbot, Long> {
    List<UserChatbot> findAllByUserId(Long user_id);
}
