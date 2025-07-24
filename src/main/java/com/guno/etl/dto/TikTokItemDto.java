// TikTokItemDto.java - TikTok Item DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TikTokItemDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sku_id")
    private String skuId;

    @JsonProperty("is_gift")
    private Boolean isGift;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("rts_time")
    private Long rtsTime;

    @JsonProperty("sku_name")
    private String skuName;

    @JsonProperty("sku_type")
    private String skuType;

    @JsonProperty("sku_image")
    private String skuImage;

    @JsonProperty("package_id")
    private String packageId;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("sale_price")
    private String salePrice;

    @JsonProperty("seller_sku")
    private String sellerSku;

    @JsonProperty("cancel_user")
    private String cancelUser;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("cancel_reason")
    private String cancelReason;

    @JsonProperty("display_status")
    private String displayStatus;

    @JsonProperty("original_price")
    private String originalPrice;

    @JsonProperty("package_status")
    private String packageStatus;

    @JsonProperty("seller_discount")
    private String sellerDiscount;

    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("platform_discount")
    private String platformDiscount;

    @JsonProperty("shipping_provider_id")
    private String shippingProviderId;

    @JsonProperty("shipping_provider_name")
    private String shippingProviderName;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }

    public Boolean getIsGift() { return isGift; }
    public void setIsGift(Boolean isGift) { this.isGift = isGift; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getRtsTime() { return rtsTime; }
    public void setRtsTime(Long rtsTime) { this.rtsTime = rtsTime; }

    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }

    public String getSkuType() { return skuType; }
    public void setSkuType(String skuType) { this.skuType = skuType; }

    public String getSkuImage() { return skuImage; }
    public void setSkuImage(String skuImage) { this.skuImage = skuImage; }

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getSalePrice() { return salePrice; }
    public void setSalePrice(String salePrice) { this.salePrice = salePrice; }

    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }

    public String getCancelUser() { return cancelUser; }
    public void setCancelUser(String cancelUser) { this.cancelUser = cancelUser; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }

    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }

    public String getPackageStatus() { return packageStatus; }
    public void setPackageStatus(String packageStatus) { this.packageStatus = packageStatus; }

    public String getSellerDiscount() { return sellerDiscount; }
    public void setSellerDiscount(String sellerDiscount) { this.sellerDiscount = sellerDiscount; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getPlatformDiscount() { return platformDiscount; }
    public void setPlatformDiscount(String platformDiscount) { this.platformDiscount = platformDiscount; }

    public String getShippingProviderId() { return shippingProviderId; }
    public void setShippingProviderId(String shippingProviderId) { this.shippingProviderId = shippingProviderId; }

    public String getShippingProviderName() { return shippingProviderName; }
    public void setShippingProviderName(String shippingProviderName) { this.shippingProviderName = shippingProviderName; }
}