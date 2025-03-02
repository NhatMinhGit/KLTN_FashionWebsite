package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.repositories.CategoryRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    @Autowired
    public ProductImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }
    @Override
    public Page<Product> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    @Override
    public Product save(ProductDto productDto) {
        // Chuyển đổi CategoryDto thành Category
        Category category = new Category();
        category.setCategoryName(productDto.getCategory().getCategoryName());
        category.setDescription(productDto.getCategory().getDescription());

        // Tạo Product mới với thông tin từ ProductDto và Category
        Product product = new Product(productDto.getName(), productDto.getPrice(), productDto.getDescription(), productDto.getStockQuantity(), category);
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {
        return null;
    }

    @Override
    public void deleteProductById(Long id) {

    }

    @Override
    public void createProduct(Product product) {

    }
}
