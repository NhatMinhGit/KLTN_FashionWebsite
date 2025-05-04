package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.repositories.CategoryRepository;
import org.example.fashion_web.backend.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return Optional.empty();
    }

    @Override
    public Category createCategory(Category category) {
        return null;
    }

    @Override
    public Category updateCategory(Category category) {
        return null;
    }

    @Override
    public void deleteCategory(Long id) {

    }

    @Override
    public void deleteAddress(Long id) {

    }

    //Lưu category
    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Optional<Category> findByName(String categoryName) {
        return categoryRepository.findByName(categoryName);
    }

    @Override
    public List<Category> getAllChildrenCategories() {
        return categoryRepository.getAllByParentCategoryNotNull();
    }

    @Override
    public List<Category> getAllParentCategories() {
        return categoryRepository.findDistinctByParentCategoryIsNull();
    }

    public List<Long> getAllSubCategoryIds(Long categoryId) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId); // Thêm chính danh mục cha vào danh sách

        List<Category> subCategories = categoryRepository.findSubCategories(categoryId);
        for (Category subCategory : subCategories) {
            categoryIds.addAll(getAllSubCategoryIds(subCategory.getId())); // Đệ quy
        }

        return categoryIds;
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Override
    public List<Long> findCategoryIdsByParentCategoryName(String name) {
        return categoryRepository.findCategoryIdsByParentCategoryName(name);
    }


}
