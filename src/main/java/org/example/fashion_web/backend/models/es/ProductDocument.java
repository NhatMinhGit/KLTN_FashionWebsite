//package org.example.fashion_web.backend.models.es;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
//import java.math.BigDecimal;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Document(indexName = "products") // Tên index trong Elasticsearch
//public class ProductDocument {
//
//    @Id
//    private Long id;
//
//    @Field(type = FieldType.Text, name = "name")
//    private String name;
//
//    @Field(type = FieldType.Text, name = "description")
//    private String description;
//
//    @Field(type = FieldType.Double, name = "price")
//    private BigDecimal price;
//
//    @Field(type = FieldType.Integer, name = "stock_quantity")
//    private Integer stockQuantity;
//
//    @Field(type = FieldType.Text, name = "categoryName")
//    private String categoryName;
//
//}