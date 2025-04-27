package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size,Long> {
//    Optional<Size> findByProductAndSizeName(Product product, String sizeName);

//    List<Size> findAllByProductId(Long id);

    List<Size> findByProductVariantId(Long variantId);


}
