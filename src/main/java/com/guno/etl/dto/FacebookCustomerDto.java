// FacebookCustomerDto.java - OPTIMIZED with String IDs
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookCustomerDto {

    @JsonProperty("id")
    private String id;  // ✅ Fixed: String for large IDs

    @JsonProperty("name")
    private String name;

    @JsonProperty("tags")
    private List<FacebookTagDto> tags;

    @JsonProperty("fb_id")
    private String fbId;  // ✅ Fixed: String for large Facebook IDs

    @JsonProperty("level")
    private String level;

    @JsonProperty("notes")
    private List<String> notes;

    @JsonProperty("emails")
    private List<String> emails;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("shop_id")
    private String shopId;  // ✅ Fixed: String for large IDs

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("is_block")
    private Boolean isBlock;

    @JsonProperty("username")
    private String username;

    @JsonProperty("creator_id")
    private String creatorId;  // ✅ Fixed: String for large IDs

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("customer_id")
    private String customerId;  // ✅ Fixed: String for large IDs

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

    @JsonProperty("referred_by_id")
    private String referredById;  // ✅ Fixed: String for large IDs

    @JsonProperty("total_revenue")
    private Integer totalRevenue;

    @JsonProperty("referral_code")
    private String referralCode;

    @JsonProperty("total_orders")
    private Integer totalOrders;

    @JsonProperty("purchased_amount")
    private Integer purchasedAmount;

    @JsonProperty("conversation_tags")
    private List<String> conversationTags;

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
    private String customerGroupId;  // ✅ Fixed: String for large IDs

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

    // ===== NESTED DTOs =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookCustomerAddressDto {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookVoucherDto {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

        @JsonProperty("code")
        private String code;

        @JsonProperty("name")
        private String name;

        @JsonProperty("discount_type")
        private String discountType;

        @JsonProperty("discount_value")
        private Integer discountValue;

        @JsonProperty("minimum_order_value")
        private Integer minimumOrderValue;

        @JsonProperty("usage_limit")
        private Integer usageLimit;

        @JsonProperty("used_count")
        private Integer usedCount;

        @JsonProperty("expires_at")
        private String expiresAt;

        @JsonProperty("is_active")
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookSocialMediaHandle {
        @JsonProperty("platform")
        private String platform;

        @JsonProperty("handle")
        private String handle;

        @JsonProperty("url")
        private String url;

        @JsonProperty("verified")
        private Boolean verified;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookTagDto {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

        @JsonProperty("name")
        private String name;

        @JsonProperty("color")
        private String color;

        @JsonProperty("description")
        private String description;
    }
}