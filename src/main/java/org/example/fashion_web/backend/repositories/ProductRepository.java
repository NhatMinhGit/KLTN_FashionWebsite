package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findProductById(Long id);
}
