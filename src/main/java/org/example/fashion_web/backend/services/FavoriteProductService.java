package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Product;

import java.util.List;

public interface FavoriteProductService {
    boolean isUserFavoritedProduct(Long userId,Long productId);

    List<Product> getFavoriteProductsByUserId(Long userId);
}
