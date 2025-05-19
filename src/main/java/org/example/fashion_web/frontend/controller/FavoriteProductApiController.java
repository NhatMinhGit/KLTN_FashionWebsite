package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.FavoriteRequest;
import org.example.fashion_web.backend.models.FavoriteProduct;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.FavoriteProductRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.FavoriteProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteProductApiController {

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private FavoriteProductService favoriteProductService;

    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        Optional<Product> productOpt = productRepository.findById(request.getProductId());

        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Product not found");
        }

        boolean exists = favoriteProductRepository.existsByUserIdAndProductId(
                request.getUserId(), request.getProductId());
        if (!exists) {
            FavoriteProduct favorite = new FavoriteProduct();
            favorite.setUser(userOpt.get());
            favorite.setProduct(productOpt.get());
            favorite.setCreatedAt(LocalDateTime.now());
            favoriteProductRepository.save(favorite);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body("Already favorited");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @RequestParam Long userId, @RequestParam Long productId) {

        boolean isFavorited = favoriteProductService.isUserFavoritedProduct(userId, productId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<?> removeFavorite(
            @RequestParam Long userId, @RequestParam Long productId) {

        favoriteProductRepository.deleteByUserIdAndProductId(userId, productId);
        return ResponseEntity.ok().build();
    }
}
