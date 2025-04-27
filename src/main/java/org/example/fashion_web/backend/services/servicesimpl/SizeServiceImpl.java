package org.example.fashion_web.backend.services.servicesimpl;

import jakarta.persistence.EntityNotFoundException;
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
    public void updateSize(Long sizeId, Size updatedSize) {
        Optional<Size> existingSizeOpt = sizeRepository.findById(sizeId);
        if (existingSizeOpt.isPresent()) {
            Size existingSize = existingSizeOpt.get();
            existingSize.setSizeName(updatedSize.getSizeName());
            existingSize.setStockQuantity(updatedSize.getStockQuantity());
            sizeRepository.save(existingSize);
        } else {
            throw new EntityNotFoundException("Size not found with id: " + sizeId);
        }
    }

    @Override
    public List<Size> findAllByProductVariantId(Long id) {
        return sizeRepository.findByProductVariantId(id);
    }

//    @Override
//    public Optional<Size> findByProductAndSizeName(Product product, String sizeName) {
//        return sizeRepository.findByProductAndSizeName(product, sizeName);
//    }

//    @Override
//    public List<Size> findAllByProductId(Long id){
//        return sizeRepository.findAllByProductId(id);
//    }
}
