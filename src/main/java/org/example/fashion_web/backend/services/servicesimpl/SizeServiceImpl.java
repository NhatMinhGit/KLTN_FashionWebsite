package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.Size;
import org.example.fashion_web.backend.repositories.SizeRepository;
import org.example.fashion_web.backend.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SizeServiceImpl implements SizeService {
    @Autowired
    private SizeRepository sizeRepository;

    @Override
    public Size save(Size size) {
        return sizeRepository.save(size);
    }

    @Override
    public List<Size> saveAll(List<Size> sizes) {
        return sizeRepository.saveAll(sizes);
    }

    @Override
    public Optional<Size> findByProductAndSizeName(Product product, String sizeName) {
        return sizeRepository.findByProductAndSizeName(product, sizeName);
    }
    @Override
    public List<Size> findAllByProductId(Long id){
        return sizeRepository.findAllByProductId(id);
    }
}
