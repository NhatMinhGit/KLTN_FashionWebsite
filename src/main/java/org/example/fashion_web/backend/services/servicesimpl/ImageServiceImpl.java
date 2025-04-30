package org.example.fashion_web.backend.services.servicesimpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FilenameUtils;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.ImageRepository;
import org.example.fashion_web.backend.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private Cloudinary cloudinary;
    @Override
    public Image save(Image image) {
        System.out.println("Lưu ảnh với thông tin: " + image);
        return imageRepository.save(image);
    }

//    @Override
//    public List<Image> getAllImages() {
//        return imageRepository.getAllImages();
//    }

    @Override
    public List<Image> findImagesByProductVariantId(Long id) {
        return imageRepository.findAllByProductVariantId(id);
    }

    @Override
    @Transactional
    public void deleteImageByImageUri(String imageUri) {
        imageRepository.deleteImageByImageUri(imageUri);
    }

    @Override
    public List<Image> findImagesByProductName(String productName) {
        return imageRepository.findAllByProductName(productName);
    }
    @Override
    public String uploadImage(MultipartFile file, String colorForImage, Product existingProduct) throws IOException {
        // Tên sản phẩm và tên file
        String productName = existingProduct.getName();
        String baseFileName = FilenameUtils.getBaseName(file.getOriginalFilename());

        // Mã hóa tên sản phẩm và màu sắc để đảm bảo không có ký tự đặc biệt
        String encodedProductName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
        String encodedColorForImage = URLEncoder.encode(colorForImage, StandardCharsets.UTF_8);

        // Tạo thư mục riêng cho mỗi màu sắc và sản phẩm
        String folderPath = "pics/uploads/" + encodedProductName + "/" + encodedColorForImage;

        // Upload lên Cloudinary
        Map uploadResult = null;
        try {
            uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "public_id", folderPath + "/" + baseFileName,
                        "overwrite", true,
                        "resource_type", "image"
                ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Trả về URL của ảnh đã upload
        return (String) uploadResult.get("secure_url");

    }

}
