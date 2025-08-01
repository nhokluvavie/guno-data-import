// FacebookItemDto.java - Facebook Item DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FacebookItemDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("note")
    private String note;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("components")
    private List<FacebookComponentDto> components;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("is_composite")
    private Boolean isComposite;

    @JsonProperty("is_wholesale")
    private Boolean isWholesale;

    @JsonProperty("note_product")
    private String noteProduct;

    @JsonProperty("variation_id")
    private String variationId;

    @JsonProperty("exchange_count")
    private Integer exchangeCount;

    @JsonProperty("return_count")
    private Integer returnCount;

    @JsonProperty("discount_rate")
    private Double discountRate;

    @JsonProperty("product_price")
    private Integer productPrice;

    @JsonProperty("retail_price")
    private Integer retailPrice;

    @JsonProperty("sale_price")
    private Integer salePrice;

    @JsonProperty("wholesale_price")
    private Integer wholesalePrice;

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

    @JsonProperty("product_weight")
    private Double productWeight;

    @JsonProperty("variation_weight")
    private Double variationWeight;

    @JsonProperty("product_image")
    private String productImage;

    @JsonProperty("variation_image")
    private String variationImage;

    @JsonProperty("product_description")
    private String productDescription;

    @JsonProperty("variation_description")
    private String variationDescription;

    @JsonProperty("product_category_id")
    private Integer productCategoryId;

    @JsonProperty("product_category_name")
    private String productCategoryName;

    @JsonProperty("product_brand_id")
    private Integer productBrandId;

    @JsonProperty("product_brand_name")
    private String productBrandName;

    @JsonProperty("product_tags")
    private List<String> productTags;

    @JsonProperty("variation_attributes")
    private List<FacebookVariationAttribute> variationAttributes;

    @JsonProperty("inventory_quantity")
    private Integer inventoryQuantity;

    @JsonProperty("reserved_quantity")
    private Integer reservedQuantity;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("status")
    private String status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public List<FacebookComponentDto> getComponents() { return components; }
    public void setComponents(List<FacebookComponentDto> components) { this.components = components; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Boolean getIsComposite() { return isComposite; }
    public void setIsComposite(Boolean isComposite) { this.isComposite = isComposite; }

    public Boolean getIsWholesale() { return isWholesale; }
    public void setIsWholesale(Boolean isWholesale) { this.isWholesale = isWholesale; }

    public String getNoteProduct() { return noteProduct; }
    public void setNoteProduct(String noteProduct) { this.noteProduct = noteProduct; }

    public String getVariationId() { return variationId; }
    public void setVariationId(String variationId) { this.variationId = variationId; }

    public Integer getExchangeCount() { return exchangeCount; }
    public void setExchangeCount(Integer exchangeCount) { this.exchangeCount = exchangeCount; }

    public Integer getReturnCount() { return returnCount; }
    public void setReturnCount(Integer returnCount) { this.returnCount = returnCount; }

    public Double getDiscountRate() { return discountRate; }
    public void setDiscountRate(Double discountRate) { this.discountRate = discountRate; }

    public Integer getProductPrice() { return productPrice; }
    public void setProductPrice(Integer productPrice) { this.productPrice = productPrice; }

    public Integer getRetailPrice() { return retailPrice; }
    public void setRetailPrice(Integer retailPrice) { this.retailPrice = retailPrice; }

    public Integer getSalePrice() { return salePrice; }
    public void setSalePrice(Integer salePrice) { this.salePrice = salePrice; }

    public Integer getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(Integer wholesalePrice) { this.wholesalePrice = wholesalePrice; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariationName() { return variationName; }
    public void setVariationName(String variationName) { this.variationName = variationName; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getVariationCode() { return variationCode; }
    public void setVariationCode(String variationCode) { this.variationCode = variationCode; }

    public String getProductBarcode() { return productBarcode; }
    public void setProductBarcode(String productBarcode) { this.productBarcode = productBarcode; }

    public String getVariationBarcode() { return variationBarcode; }
    public void setVariationBarcode(String variationBarcode) { this.variationBarcode = variationBarcode; }

    public Double getProductWeight() { return productWeight; }
    public void setProductWeight(Double productWeight) { this.productWeight = productWeight; }

    public Double getVariationWeight() { return variationWeight; }
    public void setVariationWeight(Double variationWeight) { this.variationWeight = variationWeight; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getVariationImage() { return variationImage; }
    public void setVariationImage(String variationImage) { this.variationImage = variationImage; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getVariationDescription() { return variationDescription; }
    public void setVariationDescription(String variationDescription) { this.variationDescription = variationDescription; }

    public Integer getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Integer productCategoryId) { this.productCategoryId = productCategoryId; }

    public String getProductCategoryName() { return productCategoryName; }
    public void setProductCategoryName(String productCategoryName) { this.productCategoryName = productCategoryName; }

    public Integer getProductBrandId() { return productBrandId; }
    public void setProductBrandId(Integer productBrandId) { this.productBrandId = productBrandId; }

    public String getProductBrandName() { return productBrandName; }
    public void setProductBrandName(String productBrandName) { this.productBrandName = productBrandName; }

    public List<String> getProductTags() { return productTags; }
    public void setProductTags(List<String> productTags) { this.productTags = productTags; }

    public List<FacebookVariationAttribute> getVariationAttributes() { return variationAttributes; }
    public void setVariationAttributes(List<FacebookVariationAttribute> variationAttributes) { this.variationAttributes = variationAttributes; }

    public Integer getInventoryQuantity() { return inventoryQuantity; }
    public void setInventoryQuantity(Integer inventoryQuantity) { this.inventoryQuantity = inventoryQuantity; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Supporting nested classes
    public static class FacebookComponentDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("variation_id")
        private String variationId;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("unit_price")
        private Integer unitPrice;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getVariationId() { return variationId; }
        public void setVariationId(String variationId) { this.variationId = variationId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Integer getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Integer unitPrice) { this.unitPrice = unitPrice; }
    }

    public static class FacebookVariationAttribute {
        @JsonProperty("attribute_name")
        private String attributeName;

        @JsonProperty("attribute_value")
        private String attributeValue;

        @JsonProperty("attribute_type")
        private String attributeType;

        public String getAttributeName() { return attributeName; }
        public void setAttributeName(String attributeName) { this.attributeName = attributeName; }

        public String getAttributeValue() { return attributeValue; }
        public void setAttributeValue(String attributeValue) { this.attributeValue = attributeValue; }

        public String getAttributeType() { return attributeType; }
        public void setAttributeType(String attributeType) { this.attributeType = attributeType; }
    }
}