package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.SizeInfo;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.ProductVariant;
import org.example.fashion_web.backend.models.Size;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.ProductVariantService;
import org.example.fashion_web.backend.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SizeManagementController {
    @Autowired
    private ProductVariantService productVariantService;
    @Autowired
    private SizeService sizeService;
    @Autowired
    private ImageService imageService;

//    @GetMapping("/api/sizes/{productId}/{color}")
//    @ResponseBody
//    public List<SizeInfo> getSizesByColor(@PathVariable Long productId, @PathVariable String color) {
//        List<ProductVariant> productVariants = productVariantService.findAllByProductId(productId);
//        List<SizeInfo> sizeInfos = new ArrayList<>();
//
//        for (ProductVariant variant : productVariants) {
//            if (variant.getColor().equalsIgnoreCase(color)) {
//                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
//                for (Size size : sizes) {
//                    sizeInfos.add(new SizeInfo(color, size.getSizeName(), size.getStockQuantity()));
//                }
//            }
//        }
//
//        return sizeInfos;
//    }
    @GetMapping("/api/sizes/{productId}/{color}")
    @ResponseBody
    public List<SizeInfo> getSizesByColor(@PathVariable Long productId, @PathVariable String color) {
        List<ProductVariant> productVariants = productVariantService.findAllByProductId(productId);
        List<SizeInfo> sizeInfos = new ArrayList<>();

        for (ProductVariant variant : productVariants) {
            if (variant.getColor().equalsIgnoreCase(color)) {
                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                for (Size size : sizes) {
                    sizeInfos.add(new SizeInfo(variant.getId(), color, size.getSizeName(), size.getStockQuantity()));
                }
            }
        }
        return sizeInfos;
    }


    @GetMapping("/api/images/{productId}/{color}")
    @ResponseBody
    public List<String> getImagesByColor(@PathVariable Long productId, @PathVariable String color) {
        List<ProductVariant> productVariants = productVariantService.findAllByProductId(productId);
        List<String> imageUrls = new ArrayList<>();

        for (ProductVariant variant : productVariants) {
            // Kiểm tra nếu màu của biến thể sản phẩm trùng với màu được chọn
            if (color.equalsIgnoreCase(variant.getColor())) {
                List<Image> imageList = imageService.findImagesByProductVariantId(variant.getId());
                if (imageList != null && !imageList.isEmpty()) {
                    imageList.stream()
                            .map(Image::getImageUri)
                            .filter(uri -> uri.startsWith("https://res.cloudinary.com"))
                            .forEach(imageUrls::add);
                }
            }
        }

        return imageUrls;
    }


}
