package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Image;

import java.util.List;

public interface ImageService {
    Image save(Image image);

//    List<Image> getAllImages();

    List<Image> findImagesByProductId(Long id);
}
