package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedBackRepository extends JpaRepository<Feedback,Long> {
    Optional<Feedback> findByProductId(Long id);

    List<Feedback> findByProductIdOrderByCreatedAtDesc(Long id);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
