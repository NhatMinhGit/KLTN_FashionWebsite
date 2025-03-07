package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByName(String brandName);
}
