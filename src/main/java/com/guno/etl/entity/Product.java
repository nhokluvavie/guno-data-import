package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProductId.class)
public class Product {

    @Id
    @Column(name = "sku")
    private String sku;

    @Id
    @Column(name = "platform_product_id")
    private String platformProductId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "variation_id")
    private String variationId;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "category_level_1")
    private String categoryLevel1;

    @Column(name = "category_level_2")
    private String categoryLevel2;

    @Column(name = "category_level_3")
    private String categoryLevel3;

    @Column(name = "category_path")
    private String categoryPath;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "material")
    private String material;

    @Column(name = "weight_gram", nullable = false)
    @Builder.Default
    private Integer weightGram = 0;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "cost_price", nullable = false)
    @Builder.Default
    private Double costPrice = 0.0;

    @Column(name = "retail_price", nullable = false)
    @Builder.Default
    private Double retailPrice = 0.0;

    @Column(name = "original_price", nullable = false)
    @Builder.Default
    private Double originalPrice = 0.0;

    @Column(name = "price_range")
    private String priceRange;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_seasonal", nullable = false)
    @Builder.Default
    private Boolean isSeasonal = false;

    @Column(name = "is_new_arrival", nullable = false)
    @Builder.Default
    private Boolean isNewArrival = false;

    @Column(name = "is_best_seller", nullable = false)
    @Builder.Default
    private Boolean isBestSeller = false;

    @Column(name = "primary_image_url")
    private String primaryImageUrl;

    @Column(name = "image_count", nullable = false)
    @Builder.Default
    private Integer imageCount = 0;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_keywords")
    private String seoKeywords;
}