package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    Image save(Image image);
//    List<Image> getAllImages();

    List<Image> findImagesByProductVariantId(Long id);

    void deleteImageByImageUri(String imageUri);

    List<Image> findImagesByProductName(String productName);

    String uploadImage(MultipartFile file, String colorForImage, Product existingProduct) throws IOException;
}
