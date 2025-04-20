package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByProductId(Long productId);

    @Transactional
    void deleteImageByImageUri(String imageUri);

    Optional<Image> findFirstByProduct_Id(Long productId);
}
