package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.FavoriteProduct;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.FavoriteProductRepository;
import org.example.fashion_web.backend.services.FavoriteProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteProductImpl implements FavoriteProductService {
    @Autowired
    private FavoriteProductRepository favoriteProductRepository;
    @Override
    public boolean isUserFavoritedProduct(Long userId, Long productId) {
        return favoriteProductRepository.existsByUserIdAndProductId(userId,productId);
    }

    @Override
    public List<Product> getFavoriteProductsByUserId(Long userId) {
        List<FavoriteProduct> favorites = favoriteProductRepository.findByUserId(userId);
        return favorites.stream()
                .map(FavoriteProduct::getProduct)
                .collect(Collectors.toList());
    }

}
