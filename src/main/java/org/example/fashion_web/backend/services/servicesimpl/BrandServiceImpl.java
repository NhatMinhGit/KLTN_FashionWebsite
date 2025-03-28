package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Brand;
import org.example.fashion_web.backend.repositories.BrandRepository;
import org.example.fashion_web.backend.services.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;


    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    public Optional<Brand> findByName(String brandName) {
        return brandRepository.findByName(brandName);
    }
}
