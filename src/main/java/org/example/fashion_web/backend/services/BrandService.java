package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandService {
    List<Brand> getAllBrands();

    Brand save(Brand brand);

    Optional<Brand> findByName(String brandName);
}
