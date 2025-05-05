package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findCategoryById(Long id);
    Optional<Category> findById(Long id);

    Optional<Category> findByName(String categoryName);

    List<Category> getAllByParentCategoryNotNull();
    List<Category> findDistinctByParentCategoryIsNull();

    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
    List<Category> findSubCategories(@Param("parentId") Long parentId);

    @Query("SELECT c.id FROM Category c WHERE c.parentCategory.name = :parentName")
    List<Long> findCategoryIdsByParentCategoryName(@Param("parentName") String parentName);

}
