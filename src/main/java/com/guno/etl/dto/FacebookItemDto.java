// FacebookItemDto.java - Facebook Item DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookItemDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("note")
    private String note;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("components")
    private Object components;

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

    @JsonProperty("returned_count")
    private Integer returnedCount;

    @JsonProperty("delivered_count")
    private Integer deliveredCount;

    @JsonProperty("price")
    private Long price;

    @JsonProperty("discount_type")
    private String discountType;

    @JsonProperty("discount_value")
    private Long discountValue;

    @JsonProperty("discount_amount")
    private Long discountAmount;

    @JsonProperty("total_amount")
    private Long totalAmount;

    @JsonProperty("variation_name")
    private String variationName;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("variation_code")
    private String variationCode;

    @JsonProperty("image_url")
    private String imageUrl;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Object getComponents() { return components; }
    public void setComponents(Object components) { this.components = components; }

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

    public Integer getReturnedCount() { return returnedCount; }
    public void setReturnedCount(Integer returnedCount) { this.returnedCount = returnedCount; }

    public Integer getDeliveredCount() { return deliveredCount; }
    public void setDeliveredCount(Integer deliveredCount) { this.deliveredCount = deliveredCount; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Long getDiscountValue() { return discountValue; }
    public void setDiscountValue(Long discountValue) { this.discountValue = discountValue; }

    public Long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Long discountAmount) { this.discountAmount = discountAmount; }

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }

    public String getVariationName() { return variationName; }
    public void setVariationName(String variationName) { this.variationName = variationName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getVariationCode() { return variationCode; }
    public void setVariationCode(String variationCode) { this.variationCode = variationCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}