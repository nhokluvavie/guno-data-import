// FacebookItemDto.java - OPTIMIZED with String IDs
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookItemDto {

    @JsonProperty("id")
    private String id;  // ✅ Fixed: String for large IDs

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("note")
    private String note;

    @JsonProperty("product_id")
    private String productId;  // ✅ Fixed: String for large IDs

    @JsonProperty("variation_id")
    private String variationId;  // ✅ Fixed: String for large IDs

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("variation_name")
    private String variationName;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("variation_code")
    private String variationCode;

    @JsonProperty("product_barcode")
    private String productBarcode;

    @JsonProperty("variation_barcode")
    private String variationBarcode;

    // ===== PRICING - Using BigDecimal for precision =====

    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    @JsonProperty("sale_price")
    private BigDecimal salePrice;

    @JsonProperty("wholesale_price")
    private BigDecimal wholesalePrice;

    @JsonProperty("product_price")
    private BigDecimal productPrice;

    @JsonProperty("discount_rate")
    private BigDecimal discountRate;

    // ===== INVENTORY =====

    @JsonProperty("quantity_available")
    private Integer quantityAvailable;

    @JsonProperty("quantity_committed")
    private Integer quantityCommitted;

    @JsonProperty("quantity_incoming")
    private Integer quantityIncoming;

    @JsonProperty("quantity_on_hand")
    private Integer quantityOnHand;

    // ===== PRODUCT DETAILS =====

    @JsonProperty("weight")
    private Double weight;

    @JsonProperty("product_image")
    private String productImage;

    @JsonProperty("variation_image")
    private String variationImage;

    @JsonProperty("product_description")
    private String productDescription;

    @JsonProperty("variation_description")
    private String variationDescription;

    @JsonProperty("category_id")
    private String categoryId;  // ✅ Fixed: String for large IDs

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("brand_id")
    private String brandId;  // ✅ Fixed: String for large IDs

    @JsonProperty("brand_name")
    private String brandName;

    // ===== COMPOSITE PRODUCTS =====

    @JsonProperty("is_composite")
    private Boolean isComposite;

    @JsonProperty("composite_items")
    private List<FacebookCompositeItemDto> compositeItems;

    // ===== VARIATION ATTRIBUTES =====

    @JsonProperty("variation_attributes")
    private List<FacebookVariationAttributeDto> variationAttributes;

    // ===== STATUS & TRACKING =====

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    // ===== NESTED DTOs =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookCompositeItemDto {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

        @JsonProperty("product_id")
        private String productId;  // ✅ Fixed: String for large IDs

        @JsonProperty("variation_id")
        private String variationId;  // ✅ Fixed: String for large IDs

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("name")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookVariationAttributeDto {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;

        @JsonProperty("display_name")
        private String displayName;

        @JsonProperty("display_value")
        private String displayValue;
    }
}