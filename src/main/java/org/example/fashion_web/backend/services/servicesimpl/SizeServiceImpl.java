package org.example.fashion_web.backend.services.servicesimpl;

import jakarta.persistence.EntityNotFoundException;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.Size;
import org.example.fashion_web.backend.repositories.SizeRepository;
import org.example.fashion_web.backend.services.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
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

    @Override
    public Optional<Size> findByProductVariantIdAndSizeName(Long id, String name) {
        return sizeRepository.findByProductVariantIdAndSizeName(id,name);
    }

    @Override
    public Optional<Size> getSizeIdByName(String name) {
        return sizeRepository.findBySizeName(name);
    }

    @Override
    public void updateStockQuantity(Long variantId, String sizeName, int quantity) {
        // Tìm Size tương ứng với variantId và sizeName
        Size size = sizeRepository.findByProductVariantIdAndSizeName(variantId, sizeName)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy size " + sizeName + " cho variantId " + variantId));
        // Cập nhật số lượng tồn kho
        int newStockQuantity = size.getStockQuantity() + quantity;
        size.setStockQuantity(newStockQuantity);
        // Lưu lại đối tượng Size đã cập nhật
        sizeRepository.save(size);
    }

    @Override
    public void increaseStock(Long productSizeId, int amount) {
        Size size = sizeRepository.findById(productSizeId)
                .orElseThrow(() -> new NoSuchElementException("ProductSize not found"));
        size.setStockQuantity(size.getStockQuantity() + amount);
        sizeRepository.save(size);
    }

    @Override
    public boolean decreaseStock(Long productSizeId, int amount) {
        Size size = sizeRepository.findById(productSizeId)
                .orElseThrow(() -> new NoSuchElementException("ProductSize not found"));
        if (size.getStockQuantity() < amount) return false;
        size.setStockQuantity(size.getStockQuantity() - amount);
        sizeRepository.save(size);
        return true;
    }

    @Override
    public Integer getTotalStock() {
        return sizeRepository.sumStock();
    }

    @Override
    public BigDecimal getStockValue() {
        return sizeRepository.sumStockValue();
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
