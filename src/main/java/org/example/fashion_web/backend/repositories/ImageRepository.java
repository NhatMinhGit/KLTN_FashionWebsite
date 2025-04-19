package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByProductId(Long productId);

    @Query("SELECT i.imageUri " +
            "FROM Image i " +
            "JOIN i.product p " +
            "WHERE p.name = :name")
    List<Image> findAllByProductName(String name);


    @Transactional
    void deleteImageByImageUri(String imageUri);
}
