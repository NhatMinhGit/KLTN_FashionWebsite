package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Comment;
import org.example.fashion_web.backend.repositories.CommentRepository;
import org.example.fashion_web.backend.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Override
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

}
