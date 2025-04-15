//package org.example.fashion_web.backend.repositories.es;
//
//import org.example.fashion_web.backend.models.es.ProductDocument;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
//
//    Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);
//
//    Page<ProductDocument> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);
//
//    Page<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
//
//    Page<ProductDocument> findByCategoryName(String categoryName, Pageable pageable);
//
//}