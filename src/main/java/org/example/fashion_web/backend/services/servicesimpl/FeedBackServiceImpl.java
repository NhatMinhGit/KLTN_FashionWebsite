package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Feedback;
import org.example.fashion_web.backend.repositories.FeedBackRepository;
import org.example.fashion_web.backend.services.FeedBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedBackServiceImpl implements FeedBackService {
    @Autowired
    private FeedBackRepository feedBackRepository;
    @Override
    public Optional<Feedback> findByProductId(Long id) {
        return feedBackRepository.findByProductId(id);
    }

    @Override
    public void save(Feedback feedback) {
        feedBackRepository.save(feedback);
    }

    @Override
    public List<Feedback> findByProductIdOrderByCreateAtDesc(Long id) {
        return feedBackRepository.findByProductIdOrderByCreatedAtDesc(id);
    }
}
