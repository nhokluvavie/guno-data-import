// TikTokOrderDto.java - TikTok Order DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TikTokOrderDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("shop_id")
    private String shopId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private TikTokOrderData data;

    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("update_time")
    private Long updateTime;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public TikTokOrderData getData() { return data; }
    public void setData(TikTokOrderData data) { this.data = data; }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }

    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static class TikTokOrderData {

        @JsonProperty("id")
        private String id;

        @JsonProperty("is_cod")
        private Boolean isCod;

        @JsonProperty("status")
        private String status;

        @JsonProperty("payment")
        private TikTokPayment payment;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("packages")
        private List<TikTokPackage> packages;

        @JsonProperty("rts_time")
        private Long rtsTime;

        @JsonProperty("paid_time")
        private Long paidTime;

        @JsonProperty("line_items")
        private List<TikTokItemDto> lineItems;

        @JsonProperty("buyer_email")
        private String buyerEmail;

        @JsonProperty("create_time")
        private Long createTime;

        @JsonProperty("update_time")
        private Long updateTime;

        @JsonProperty("rts_sla_time")
        private Long rtsSlaTime;

        @JsonProperty("tts_sla_time")
        private Long ttsSlaTime;

        @JsonProperty("warehouse_id")
        private String warehouseId;

        @JsonProperty("buyer_message")
        private String buyerMessage;

        @JsonProperty("cancel_reason")
        private String cancelReason;

        @JsonProperty("delivery_time")
        private Long deliveryTime;

        @JsonProperty("delivery_type")
        private String deliveryType;

        @JsonProperty("shipping_type")
        private String shippingType;

        @JsonProperty("collection_time")
        private Long collectionTime;

        @JsonProperty("is_sample_order")
        private Boolean isSampleOrder;

        @JsonProperty("tracking_number")
        private String trackingNumber;

        @JsonProperty("fulfillment_type")
        private String fulfillmentType;

        @JsonProperty("is_on_hold_order")
        private Boolean isOnHoldOrder;

        @JsonProperty("commerce_platform")
        private String commercePlatform;

        @JsonProperty("recipient_address")
        private TikTokRecipientAddress recipientAddress;

        @JsonProperty("shipping_due_time")
        private Long shippingDueTime;

        @JsonProperty("shipping_provider")
        private String shippingProvider;

        @JsonProperty("delivery_option_id")
        private String deliveryOptionId;

        @JsonProperty("collection_due_time")
        private Long collectionDueTime;

        @JsonProperty("payment_method_name")
        private String paymentMethodName;

        @JsonProperty("delivery_option_name")
        private String deliveryOptionName;

        @JsonProperty("is_replacement_order")
        private Boolean isReplacementOrder;

        @JsonProperty("shipping_provider_id")
        private String shippingProviderId;

        @JsonProperty("cancel_order_sla_time")
        private Long cancelOrderSlaTime;

        @JsonProperty("cancellation_initiator")
        private String cancellationInitiator;

        @JsonProperty("fast_dispatch_sla_time")
        private Long fastDispatchSlaTime;

        @JsonProperty("has_updated_recipient_address")
        private Boolean hasUpdatedRecipientAddress;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Boolean getIsCod() { return isCod; }
        public void setIsCod(Boolean isCod) { this.isCod = isCod; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public TikTokPayment getPayment() { return payment; }
        public void setPayment(TikTokPayment payment) { this.payment = payment; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public List<TikTokPackage> getPackages() { return packages; }
        public void setPackages(List<TikTokPackage> packages) { this.packages = packages; }

        public Long getRtsTime() { return rtsTime; }
        public void setRtsTime(Long rtsTime) { this.rtsTime = rtsTime; }

        public Long getPaidTime() { return paidTime; }
        public void setPaidTime(Long paidTime) { this.paidTime = paidTime; }

        public List<TikTokItemDto> getLineItems() { return lineItems; }
        public void setLineItems(List<TikTokItemDto> lineItems) { this.lineItems = lineItems; }

        public String getBuyerEmail() { return buyerEmail; }
        public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

        public Long getCreateTime() { return createTime; }
        public void setCreateTime(Long createTime) { this.createTime = createTime; }

        public Long getUpdateTime() { return updateTime; }
        public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

        public Long getRtsSlaTime() { return rtsSlaTime; }
        public void setRtsSlaTime(Long rtsSlaTime) { this.rtsSlaTime = rtsSlaTime; }

        public Long getTtsSlaTime() { return ttsSlaTime; }
        public void setTtsSlaTime(Long ttsSlaTime) { this.ttsSlaTime = ttsSlaTime; }

        public String getWarehouseId() { return warehouseId; }
        public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

        public String getBuyerMessage() { return buyerMessage; }
        public void setBuyerMessage(String buyerMessage) { this.buyerMessage = buyerMessage; }

        public String getCancelReason() { return cancelReason; }
        public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

        public Long getDeliveryTime() { return deliveryTime; }
        public void setDeliveryTime(Long deliveryTime) { this.deliveryTime = deliveryTime; }

        public String getDeliveryType() { return deliveryType; }
        public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

        public String getShippingType() { return shippingType; }
        public void setShippingType(String shippingType) { this.shippingType = shippingType; }

        public Long getCollectionTime() { return collectionTime; }
        public void setCollectionTime(Long collectionTime) { this.collectionTime = collectionTime; }

        public Boolean getIsSampleOrder() { return isSampleOrder; }
        public void setIsSampleOrder(Boolean isSampleOrder) { this.isSampleOrder = isSampleOrder; }

        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

        public String getFulfillmentType() { return fulfillmentType; }
        public void setFulfillmentType(String fulfillmentType) { this.fulfillmentType = fulfillmentType; }

        public Boolean getIsOnHoldOrder() { return isOnHoldOrder; }
        public void setIsOnHoldOrder(Boolean isOnHoldOrder) { this.isOnHoldOrder = isOnHoldOrder; }

        public String getCommercePlatform() { return commercePlatform; }
        public void setCommercePlatform(String commercePlatform) { this.commercePlatform = commercePlatform; }

        public TikTokRecipientAddress getRecipientAddress() { return recipientAddress; }
        public void setRecipientAddress(TikTokRecipientAddress recipientAddress) { this.recipientAddress = recipientAddress; }

        public String getShippingProvider() { return shippingProvider; }
        public void setShippingProvider(String shippingProvider) { this.shippingProvider = shippingProvider; }

        public String getPaymentMethodName() { return paymentMethodName; }
        public void setPaymentMethodName(String paymentMethodName) { this.paymentMethodName = paymentMethodName; }

        // Additional getters/setters for remaining fields...
        public Long getShippingDueTime() { return shippingDueTime; }
        public void setShippingDueTime(Long shippingDueTime) { this.shippingDueTime = shippingDueTime; }

        public String getDeliveryOptionId() { return deliveryOptionId; }
        public void setDeliveryOptionId(String deliveryOptionId) { this.deliveryOptionId = deliveryOptionId; }

        public Long getCollectionDueTime() { return collectionDueTime; }
        public void setCollectionDueTime(Long collectionDueTime) { this.collectionDueTime = collectionDueTime; }

        public String getDeliveryOptionName() { return deliveryOptionName; }
        public void setDeliveryOptionName(String deliveryOptionName) { this.deliveryOptionName = deliveryOptionName; }

        public Boolean getIsReplacementOrder() { return isReplacementOrder; }
        public void setIsReplacementOrder(Boolean isReplacementOrder) { this.isReplacementOrder = isReplacementOrder; }

        public String getShippingProviderId() { return shippingProviderId; }
        public void setShippingProviderId(String shippingProviderId) { this.shippingProviderId = shippingProviderId; }

        public Long getCancelOrderSlaTime() { return cancelOrderSlaTime; }
        public void setCancelOrderSlaTime(Long cancelOrderSlaTime) { this.cancelOrderSlaTime = cancelOrderSlaTime; }

        public String getCancellationInitiator() { return cancellationInitiator; }
        public void setCancellationInitiator(String cancellationInitiator) { this.cancellationInitiator = cancellationInitiator; }

        public Long getFastDispatchSlaTime() { return fastDispatchSlaTime; }
        public void setFastDispatchSlaTime(Long fastDispatchSlaTime) { this.fastDispatchSlaTime = fastDispatchSlaTime; }

        public Boolean getHasUpdatedRecipientAddress() { return hasUpdatedRecipientAddress; }
        public void setHasUpdatedRecipientAddress(Boolean hasUpdatedRecipientAddress) { this.hasUpdatedRecipientAddress = hasUpdatedRecipientAddress; }
    }

    public static class TikTokPayment {
        @JsonProperty("tax")
        private String tax;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("sub_total")
        private String subTotal;

        @JsonProperty("shipping_fee")
        private String shippingFee;

        @JsonProperty("total_amount")
        private String totalAmount;

        @JsonProperty("seller_discount")
        private String sellerDiscount;

        @JsonProperty("platform_discount")
        private String platformDiscount;

        @JsonProperty("original_shipping_fee")
        private String originalShippingFee;

        @JsonProperty("original_total_product_price")
        private String originalTotalProductPrice;

        @JsonProperty("shipping_fee_seller_discount")
        private String shippingFeeSellerDiscount;

        @JsonProperty("shipping_fee_cofunded_discount")
        private String shippingFeeCofundedDiscount;

        @JsonProperty("shipping_fee_platform_discount")
        private String shippingFeePlatformDiscount;

        // Getters and setters
        public String getTax() { return tax; }
        public void setTax(String tax) { this.tax = tax; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getSubTotal() { return subTotal; }
        public void setSubTotal(String subTotal) { this.subTotal = subTotal; }

        public String getShippingFee() { return shippingFee; }
        public void setShippingFee(String shippingFee) { this.shippingFee = shippingFee; }

        public String getTotalAmount() { return totalAmount; }
        public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }

        public String getSellerDiscount() { return sellerDiscount; }
        public void setSellerDiscount(String sellerDiscount) { this.sellerDiscount = sellerDiscount; }

        public String getPlatformDiscount() { return platformDiscount; }
        public void setPlatformDiscount(String platformDiscount) { this.platformDiscount = platformDiscount; }

        public String getOriginalShippingFee() { return originalShippingFee; }
        public void setOriginalShippingFee(String originalShippingFee) { this.originalShippingFee = originalShippingFee; }

        public String getOriginalTotalProductPrice() { return originalTotalProductPrice; }
        public void setOriginalTotalProductPrice(String originalTotalProductPrice) { this.originalTotalProductPrice = originalTotalProductPrice; }

        public String getShippingFeeSellerDiscount() { return shippingFeeSellerDiscount; }
        public void setShippingFeeSellerDiscount(String shippingFeeSellerDiscount) { this.shippingFeeSellerDiscount = shippingFeeSellerDiscount; }

        public String getShippingFeeCofundedDiscount() { return shippingFeeCofundedDiscount; }
        public void setShippingFeeCofundedDiscount(String shippingFeeCofundedDiscount) { this.shippingFeeCofundedDiscount = shippingFeeCofundedDiscount; }

        public String getShippingFeePlatformDiscount() { return shippingFeePlatformDiscount; }
        public void setShippingFeePlatformDiscount(String shippingFeePlatformDiscount) { this.shippingFeePlatformDiscount = shippingFeePlatformDiscount; }
    }

    public static class TikTokPackage {
        @JsonProperty("id")
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    public static class TikTokRecipientAddress {
        @JsonProperty("name")
        private String name;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("postal_code")
        private String postalCode;

        @JsonProperty("region_code")
        private String regionCode;

        @JsonProperty("full_address")
        private String fullAddress;

        @JsonProperty("phone_number")
        private String phoneNumber;

        @JsonProperty("address_line1")
        private String addressLine1;

        @JsonProperty("address_line2")
        private String addressLine2;

        @JsonProperty("address_line3")
        private String addressLine3;

        @JsonProperty("address_line4")
        private String addressLine4;

        @JsonProperty("district_info")
        private List<TikTokDistrictInfo> districtInfo;

        @JsonProperty("address_detail")
        private String addressDetail;

        @JsonProperty("last_name_local_script")
        private String lastNameLocalScript;

        @JsonProperty("first_name_local_script")
        private String firstNameLocalScript;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getRegionCode() { return regionCode; }
        public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

        public String getFullAddress() { return fullAddress; }
        public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

        public String getAddressLine3() { return addressLine3; }
        public void setAddressLine3(String addressLine3) { this.addressLine3 = addressLine3; }

        public String getAddressLine4() { return addressLine4; }
        public void setAddressLine4(String addressLine4) { this.addressLine4 = addressLine4; }

        public List<TikTokDistrictInfo> getDistrictInfo() { return districtInfo; }
        public void setDistrictInfo(List<TikTokDistrictInfo> districtInfo) { this.districtInfo = districtInfo; }

        public String getAddressDetail() { return addressDetail; }
        public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

        public String getLastNameLocalScript() { return lastNameLocalScript; }
        public void setLastNameLocalScript(String lastNameLocalScript) { this.lastNameLocalScript = lastNameLocalScript; }

        public String getFirstNameLocalScript() { return firstNameLocalScript; }
        public void setFirstNameLocalScript(String firstNameLocalScript) { this.firstNameLocalScript = firstNameLocalScript; }
    }

    public static class TikTokDistrictInfo {
        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("address_level")
        private String addressLevel;

        @JsonProperty("address_level_name")
        private String addressLevelName;

        // Getters and setters
        public String getAddressName() { return addressName; }
        public void setAddressName(String addressName) { this.addressName = addressName; }

        public String getAddressLevel() { return addressLevel; }
        public void setAddressLevel(String addressLevel) { this.addressLevel = addressLevel; }

        public String getAddressLevelName() { return addressLevelName; }
        public void setAddressLevelName(String addressLevelName) { this.addressLevelName = addressLevelName; }
    }
}