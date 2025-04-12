package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.ImageRepository;
import org.example.fashion_web.backend.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Override
    @Transactional
    public void deleteImageByImageUri(String imageUri) {
        imageRepository.deleteImageByImageUri(imageUri);
    }



}
