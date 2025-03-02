package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.repositories.CategoryRepository;
import org.example.fashion_web.backend.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id){
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(Category address){
        return categoryRepository.save(address);
    }

    @Override
    public Category updateCategory(Category category){
        if(category.getId() == null){
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        // Kiểm tra xem địa chỉ có tồn tại hay không
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for this id :: " + category.getId()));

        // Cập nhật các trường cần thiết
        existingCategory.setCategoryName(category.getCategoryName());
        existingCategory.setDescription(category.getDescription());

        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(Long id) {

    }

    @Override
    public void deleteAddress(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for this id :: " + id));
        categoryRepository.delete(category);
    }
}
