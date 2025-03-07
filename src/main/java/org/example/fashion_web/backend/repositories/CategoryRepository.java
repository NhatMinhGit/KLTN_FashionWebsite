package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findCategoryById(Long id);

    Optional<Category> findByName(String categoryName);

    List<Category> getAllByParentCategoryNotNull();
    List<Category> findDistinctByParentCategoryIsNull();
}
