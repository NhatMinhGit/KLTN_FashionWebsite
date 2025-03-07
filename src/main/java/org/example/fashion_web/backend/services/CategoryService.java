package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Category createCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Long id);

    void deleteAddress(Long id);

    Category save(Category category);

    Optional<Category> findByName(String categoryName);

    List<Category> getAllChildrenCategories();
    List<Category> getAllParentCategories();
}
