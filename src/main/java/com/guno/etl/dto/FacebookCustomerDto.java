// FacebookCustomerDto.java - Facebook Customer DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FacebookCustomerDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("tags")
    private List<FacebookTagDto> tags;

    @JsonProperty("fb_id")
    private String fbId;

    @JsonProperty("level")
    private String level;

    @JsonProperty("notes")
    private List<String> notes;

    @JsonProperty("emails")
    private List<String> emails;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("is_block")
    private Boolean isBlock;

    @JsonProperty("username")
    private String username;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("inserted_at")
    private String insertedAt;

    @JsonProperty("order_count")
    private Integer orderCount;

    @JsonProperty("list_voucher")
    private List<FacebookVoucherDto> listVoucher;

    @JsonProperty("reward_point")
    private Integer rewardPoint;

    @JsonProperty("current_debts")
    private Integer currentDebts;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("last_order_at")
    private String lastOrderAt;

    @JsonProperty("order_sources")
    private List<String> orderSources;

    @JsonProperty("phone_numbers")
    private List<String> phoneNumbers;

    @JsonProperty("referral_code")
    private String referralCode;

    @JsonProperty("user_block_id")
    private String userBlockId;

    @JsonProperty("count_referrals")
    private Integer countReferrals;

    @JsonProperty("is_adjust_debts")
    private Boolean isAdjustDebts;

    @JsonProperty("assigned_user_id")
    private String assignedUserId;

    @JsonProperty("purchased_amount")
    private Integer purchasedAmount;

    @JsonProperty("active_levera_pay")
    private Boolean activeLeveraPay;

    @JsonProperty("conversation_tags")
    private String conversationTags;

    @JsonProperty("used_reward_point")
    private Integer usedRewardPoint;

    @JsonProperty("succeed_order_count")
    private Integer succeedOrderCount;

    @JsonProperty("is_discount_by_level")
    private Boolean isDiscountByLevel;

    @JsonProperty("returned_order_count")
    private Integer returnedOrderCount;

    @JsonProperty("total_amount_referred")
    private Integer totalAmountReferred;

    @JsonProperty("shop_customer_addresses")
    private List<FacebookCustomerAddressDto> shopCustomerAddresses;

    @JsonProperty("customer_group_id")
    private Integer customerGroupId;

    @JsonProperty("customer_group_name")
    private String customerGroupName;

    @JsonProperty("customer_type")
    private String customerType;

    @JsonProperty("registration_source")
    private String registrationSource;

    @JsonProperty("first_order_date")
    private String firstOrderDate;

    @JsonProperty("average_order_value")
    private Double averageOrderValue;

    @JsonProperty("lifetime_value")
    private Double lifetimeValue;

    @JsonProperty("last_interaction_date")
    private String lastInteractionDate;

    @JsonProperty("preferred_contact_method")
    private String preferredContactMethod;

    @JsonProperty("marketing_consent")
    private Boolean marketingConsent;

    @JsonProperty("sms_consent")
    private Boolean smsConsent;

    @JsonProperty("email_consent")
    private Boolean emailConsent;

    @JsonProperty("loyalty_tier")
    private String loyaltyTier;

    @JsonProperty("social_media_handles")
    private List<FacebookSocialMediaHandle> socialMediaHandles;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<FacebookTagDto> getTags() { return tags; }
    public void setTags(List<FacebookTagDto> tags) { this.tags = tags; }

    public String getFbId() { return fbId; }
    public void setFbId(String fbId) { this.fbId = fbId; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getIsBlock() { return isBlock; }
    public void setIsBlock(Boolean isBlock) { this.isBlock = isBlock; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getInsertedAt() { return insertedAt; }
    public void setInsertedAt(String insertedAt) { this.insertedAt = insertedAt; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

    public List<FacebookVoucherDto> getListVoucher() { return listVoucher; }
    public void setListVoucher(List<FacebookVoucherDto> listVoucher) { this.listVoucher = listVoucher; }

    public Integer getRewardPoint() { return rewardPoint; }
    public void setRewardPoint(Integer rewardPoint) { this.rewardPoint = rewardPoint; }

    public Integer getCurrentDebts() { return currentDebts; }
    public void setCurrentDebts(Integer currentDebts) { this.currentDebts = currentDebts; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getLastOrderAt() { return lastOrderAt; }
    public void setLastOrderAt(String lastOrderAt) { this.lastOrderAt = lastOrderAt; }

    public List<String> getOrderSources() { return orderSources; }
    public void setOrderSources(List<String> orderSources) { this.orderSources = orderSources; }

    public List<String> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public String getUserBlockId() { return userBlockId; }
    public void setUserBlockId(String userBlockId) { this.userBlockId = userBlockId; }

    public Integer getCountReferrals() { return countReferrals; }
    public void setCountReferrals(Integer countReferrals) { this.countReferrals = countReferrals; }

    public Boolean getIsAdjustDebts() { return isAdjustDebts; }
    public void setIsAdjustDebts(Boolean isAdjustDebts) { this.isAdjustDebts = isAdjustDebts; }

    public String getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(String assignedUserId) { this.assignedUserId = assignedUserId; }

    public Integer getPurchasedAmount() { return purchasedAmount; }
    public void setPurchasedAmount(Integer purchasedAmount) { this.purchasedAmount = purchasedAmount; }

    public Boolean getActiveLeveraPay() { return activeLeveraPay; }
    public void setActiveLeveraPay(Boolean activeLeveraPay) { this.activeLeveraPay = activeLeveraPay; }

    public String getConversationTags() { return conversationTags; }
    public void setConversationTags(String conversationTags) { this.conversationTags = conversationTags; }

    public Integer getUsedRewardPoint() { return usedRewardPoint; }
    public void setUsedRewardPoint(Integer usedRewardPoint) { this.usedRewardPoint = usedRewardPoint; }

    public Integer getSucceedOrderCount() { return succeedOrderCount; }
    public void setSucceedOrderCount(Integer succeedOrderCount) { this.succeedOrderCount = succeedOrderCount; }

    public Boolean getIsDiscountByLevel() { return isDiscountByLevel; }
    public void setIsDiscountByLevel(Boolean isDiscountByLevel) { this.isDiscountByLevel = isDiscountByLevel; }

    public Integer getReturnedOrderCount() { return returnedOrderCount; }
    public void setReturnedOrderCount(Integer returnedOrderCount) { this.returnedOrderCount = returnedOrderCount; }

    public Integer getTotalAmountReferred() { return totalAmountReferred; }
    public void setTotalAmountReferred(Integer totalAmountReferred) { this.totalAmountReferred = totalAmountReferred; }

    public List<FacebookCustomerAddressDto> getShopCustomerAddresses() { return shopCustomerAddresses; }
    public void setShopCustomerAddresses(List<FacebookCustomerAddressDto> shopCustomerAddresses) { this.shopCustomerAddresses = shopCustomerAddresses; }

    public Integer getCustomerGroupId() { return customerGroupId; }
    public void setCustomerGroupId(Integer customerGroupId) { this.customerGroupId = customerGroupId; }

    public String getCustomerGroupName() { return customerGroupName; }
    public void setCustomerGroupName(String customerGroupName) { this.customerGroupName = customerGroupName; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public String getRegistrationSource() { return registrationSource; }
    public void setRegistrationSource(String registrationSource) { this.registrationSource = registrationSource; }

    public String getFirstOrderDate() { return firstOrderDate; }
    public void setFirstOrderDate(String firstOrderDate) { this.firstOrderDate = firstOrderDate; }

    public Double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(Double lifetimeValue) { this.lifetimeValue = lifetimeValue; }

    public String getLastInteractionDate() { return lastInteractionDate; }
    public void setLastInteractionDate(String lastInteractionDate) { this.lastInteractionDate = lastInteractionDate; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public Boolean getMarketingConsent() { return marketingConsent; }
    public void setMarketingConsent(Boolean marketingConsent) { this.marketingConsent = marketingConsent; }

    public Boolean getSmsConsent() { return smsConsent; }
    public void setSmsConsent(Boolean smsConsent) { this.smsConsent = smsConsent; }

    public Boolean getEmailConsent() { return emailConsent; }
    public void setEmailConsent(Boolean emailConsent) { this.emailConsent = emailConsent; }

    public String getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(String loyaltyTier) { this.loyaltyTier = loyaltyTier; }

    public List<FacebookSocialMediaHandle> getSocialMediaHandles() { return socialMediaHandles; }
    public void setSocialMediaHandles(List<FacebookSocialMediaHandle> socialMediaHandles) { this.socialMediaHandles = socialMediaHandles; }

    // Supporting nested classes
    public static class FacebookCustomerAddressDto {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("email")
        private String email;

        @JsonProperty("address")
        private String address;

        @JsonProperty("ward")
        private String ward;

        @JsonProperty("district")
        private String district;

        @JsonProperty("province")
        private String province;

        @JsonProperty("country")
        private String country;

        @JsonProperty("postal_code")
        private String postalCode;

        @JsonProperty("is_default")
        private Boolean isDefault;

        @JsonProperty("address_type")
        private String addressType;

        @JsonProperty("company_name")
        private String companyName;

        @JsonProperty("tax_code")
        private String taxCode;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }

        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

        public String getAddressType() { return addressType; }
        public void setAddressType(String addressType) { this.addressType = addressType; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getTaxCode() { return taxCode; }
        public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class FacebookVoucherDto {
        @JsonProperty("id")
        private String id;

        @JsonProperty("code")
        private String code;

        @JsonProperty("name")
        private String name;

        @JsonProperty("discount_type")
        private String discountType;

        @JsonProperty("discount_value")
        private Double discountValue;

        @JsonProperty("min_order_value")
        private Double minOrderValue;

        @JsonProperty("max_discount")
        private Double maxDiscount;

        @JsonProperty("usage_limit")
        private Integer usageLimit;

        @JsonProperty("used_count")
        private Integer usedCount;

        @JsonProperty("expiry_date")
        private String expiryDate;

        @JsonProperty("is_active")
        private Boolean isActive;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDiscountType() { return discountType; }
        public void setDiscountType(String discountType) { this.discountType = discountType; }

        public Double getDiscountValue() { return discountValue; }
        public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

        public Double getMinOrderValue() { return minOrderValue; }
        public void setMinOrderValue(Double minOrderValue) { this.minOrderValue = minOrderValue; }

        public Double getMaxDiscount() { return maxDiscount; }
        public void setMaxDiscount(Double maxDiscount) { this.maxDiscount = maxDiscount; }

        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

        public Integer getUsedCount() { return usedCount; }
        public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    public static class FacebookSocialMediaHandle {
        @JsonProperty("platform")
        private String platform;

        @JsonProperty("handle")
        private String handle;

        @JsonProperty("url")
        private String url;

        @JsonProperty("verified")
        private Boolean verified;

        // Getters and setters
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public String getHandle() { return handle; }
        public void setHandle(String handle) { this.handle = handle; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public Boolean getVerified() { return verified; }
        public void setVerified(Boolean verified) { this.verified = verified; }
    }

    // Reuse FacebookTagDto from FacebookOrderDto
    public static class FacebookTagDto {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}