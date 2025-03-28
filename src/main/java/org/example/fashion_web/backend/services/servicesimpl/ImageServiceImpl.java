package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.repositories.ImageRepository;
import org.example.fashion_web.backend.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    private ImageRepository imageRepository;
    @Override
    public Image save(Image image) {
        return imageRepository.save(image);
    }

//    @Override
//    public List<Image> getAllImages() {
//        return imageRepository.getAllImages();
//    }

    @Override
    public List<Image> findImagesByProductId(Long id) {
        return imageRepository.findAllByProductId(id);
    }
}
