package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedBackService {
    Optional<Feedback> findByProductId(Long id);

    void save(Feedback feedback);
    List<Feedback> findByProductIdOrderByCreateAtDesc(Long id);
}
