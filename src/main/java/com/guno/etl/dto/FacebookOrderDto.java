package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookOrderDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("nhanh_app_id")
    private Long nhanhAppId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("data")
    private FacebookOrderData data;

    @JsonProperty("nhanh_order_id")
    private Long nhanhOrderId;

    @JsonProperty("source")
    private String source;

    @JsonProperty("last_synced")
    private String lastSynced;

    @JsonProperty("inserted_at")
    private String insertedAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("createdAt")
    private String createdAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getNhanhAppId() { return nhanhAppId; }
    public void setNhanhAppId(Long nhanhAppId) { this.nhanhAppId = nhanhAppId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public FacebookOrderData getData() { return data; }
    public void setData(FacebookOrderData data) { this.data = data; }

    public String getCreatedAt() { return createdAt != null ? createdAt : insertedAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt != null ? updatedAt : updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookOrderData {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("cod")
        private Long cod;

        @JsonProperty("tax")
        private Long tax;

        @JsonProperty("cash")
        private Long cash;

        @JsonProperty("link")
        private String link;

        @JsonProperty("note")
        private String note;

        @JsonProperty("page")
        private FacebookPage page;

        @JsonProperty("tags")
        private List<FacebookTag> tags;

        @JsonProperty("type")
        private String type;

        @JsonProperty("ad_id")
        private String adId;

        @JsonProperty("items")
        private List<FacebookItemDto> items;

        // CORRECTED: Use actual Facebook field names
        @JsonProperty("total_price_after_sub_discount")
        private Long totalPriceAfterSubDiscount;

        @JsonProperty("discount")
        private Long discount;

        @JsonProperty("shipping_fee")
        private Long shippingFee;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("customer")
        private FacebookCustomer customer;

        // Facebook uses different address structure
        @JsonProperty("bill_phone_number")
        private String billPhoneNumber;

        @JsonProperty("new_province_name")
        private String newProvinceName;

        @JsonProperty("new_district_name")
        private String newDistrictName;

        @JsonProperty("new_commune_id")
        private String newCommuneId;

        @JsonProperty("new_province_id")
        private String newProvinceId;

        @JsonProperty("new_full_address")
        private String newFullAddress;

        @JsonProperty("order_sources_name")
        private String orderSourcesName;

        @JsonProperty("bill_email")
        private String billEmail;

        @JsonProperty("surcharge")
        private Long surcharge;

        @JsonProperty("system_id")
        private Long systemId;

        @JsonProperty("note_print")
        private String notePrint;

        @JsonProperty("time_assign_seller")
        private String timeAssignSeller;

        @JsonProperty("assigning_seller_id")
        private String assigningSellerId;

        @JsonProperty("time_send_partner")
        private String timeSendPartner;

        @JsonProperty("buyer_total_amount")
        private Long buyerTotalAmount;

        @JsonProperty("link_confirm_order")
        private String linkConfirmOrder;

        @JsonProperty("estimate_delivery_date")
        private String estimateDeliveryDate;

        @JsonProperty("is_exchange_order")
        private Boolean isExchangeOrder;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCod() { return cod; }
        public void setCod(Long cod) { this.cod = cod; }

        public Long getTax() { return tax; }
        public void setTax(Long tax) { this.tax = tax; }

        public Long getCash() { return cash; }
        public void setCash(Long cash) { this.cash = cash; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public FacebookPage getPage() { return page; }
        public void setPage(FacebookPage page) { this.page = page; }

        public List<FacebookTag> getTags() { return tags; }
        public void setTags(List<FacebookTag> tags) { this.tags = tags; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getAdId() { return adId; }
        public void setAdId(String adId) { this.adId = adId; }

        public List<FacebookItemDto> getItems() { return items; }
        public void setItems(List<FacebookItemDto> items) { this.items = items; }

        // CORRECTED: Map to actual Facebook field
        public Long getTotalPriceAfterSubDiscount() { return totalPriceAfterSubDiscount; }
        public void setTotalPriceAfterSubDiscount(Long totalPriceAfterSubDiscount) { this.totalPriceAfterSubDiscount = totalPriceAfterSubDiscount; }

        public Long getDiscount() { return discount; }
        public void setDiscount(Long discount) { this.discount = discount; }

        public Long getShippingFee() { return shippingFee; }
        public void setShippingFee(Long shippingFee) { this.shippingFee = shippingFee; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public FacebookCustomer getCustomer() { return customer; }
        public void setCustomer(FacebookCustomer customer) { this.customer = customer; }

        // Address methods - Facebook has different structure
        public String getBillPhoneNumber() { return billPhoneNumber; }
        public void setBillPhoneNumber(String billPhoneNumber) { this.billPhoneNumber = billPhoneNumber; }

        public String getNewProvinceName() { return newProvinceName; }
        public void setNewProvinceName(String newProvinceName) { this.newProvinceName = newProvinceName; }

        public String getNewDistrictName() { return newDistrictName; }
        public void setNewDistrictName(String newDistrictName) { this.newDistrictName = newDistrictName; }

        public String getNewFullAddress() { return newFullAddress; }
        public void setNewFullAddress(String newFullAddress) { this.newFullAddress = newFullAddress; }

        public String getOrderSourcesName() { return orderSourcesName; }
        public void setOrderSourcesName(String orderSourcesName) { this.orderSourcesName = orderSourcesName; }

        // Convenience methods for compatibility
        public String getOrderSource() { return orderSourcesName; }
        public String getDeliveryStatus() {
            // Extract delivery status from tags if available
            if (tags != null) {
                for (FacebookTag tag : tags) {
                    if (tag.getName() != null) {
                        String tagName = tag.getName().toLowerCase();
                        if (tagName.contains("delivery") || tagName.contains("shipped") ||
                                tagName.contains("picked up") || tagName.contains("on delivery")) {
                            return tag.getName();
                        }
                    }
                }
            }
            return "Unknown";
        }

        // Facebook doesn't have a single shipping_address object, so create one from fields
        public FacebookShippingAddress getShippingAddress() {
            FacebookShippingAddress address = new FacebookShippingAddress();
            address.setPhone(billPhoneNumber);
            address.setCityName(newProvinceName);
            address.setDistrictName(newDistrictName);
            address.setFullAddress(newFullAddress);
            return address;
        }

        public String getPaymentMethod() {
            if (cod != null && cod > 0) {
                return "COD";
            } else if (cash != null && cash > 0) {
                return "CASH";
            }
            return "UNKNOWN";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookPage {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("username")
        private String username;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookTag {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("name")
        private String name;

        // Getters and setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookCustomer {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("tags")
        private List<Object> tags;

        @JsonProperty("fb_id")
        private String fbId;

        @JsonProperty("level")
        private String level;

        @JsonProperty("notes")
        private List<Object> notes;

        @JsonProperty("emails")
        private List<String> emails;

        @JsonProperty("gender")
        private String gender;

        @JsonProperty("shop_id")
        private Long shopId;

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
        private List<Object> listVoucher;

        @JsonProperty("reward_point")
        private Integer rewardPoint;

        @JsonProperty("current_debts")
        private Long currentDebts;

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
        private Long purchasedAmount;

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
        private Long totalAmountReferred;

        @JsonProperty("shop_customer_addresses")
        private List<FacebookShippingAddress> shopCustomerAddresses;

        // Getters and setters (showing key ones)
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

        public Integer getRewardPoint() { return rewardPoint; }
        public void setRewardPoint(Integer rewardPoint) { this.rewardPoint = rewardPoint; }

        public Long getPurchasedAmount() { return purchasedAmount; }
        public void setPurchasedAmount(Long purchasedAmount) { this.purchasedAmount = purchasedAmount; }

        public List<String> getPhoneNumbers() { return phoneNumbers; }
        public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

        public List<String> getEmails() { return emails; }
        public void setEmails(List<String> emails) { this.emails = emails; }

        public String getReferralCode() { return referralCode; }
        public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

        public Integer getCountReferrals() { return countReferrals; }
        public void setCountReferrals(Integer countReferrals) { this.countReferrals = countReferrals; }

        public String getInsertedAt() { return insertedAt; }
        public void setInsertedAt(String insertedAt) { this.insertedAt = insertedAt; }

        public String getLastOrderAt() { return lastOrderAt; }
        public void setLastOrderAt(String lastOrderAt) { this.lastOrderAt = lastOrderAt; }

        public List<FacebookShippingAddress> getShopCustomerAddresses() { return shopCustomerAddresses; }
        public void setShopCustomerAddresses(List<FacebookShippingAddress> shopCustomerAddresses) { this.shopCustomerAddresses = shopCustomerAddresses; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FacebookShippingAddress {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("address")
        private String address;

        @JsonProperty("city_id")
        private Integer cityId;

        @JsonProperty("city_name")
        private String cityName;

        @JsonProperty("district_id")
        private Integer districtId;

        @JsonProperty("district_name")
        private String districtName;

        @JsonProperty("ward_id")
        private Integer wardId;

        @JsonProperty("ward_name")
        private String wardName;

        @JsonProperty("country_id")
        private Integer countryId;

        @JsonProperty("country_name")
        private String countryName;

        @JsonProperty("is_default")
        private Boolean isDefault;

        @JsonProperty("full_address")
        private String fullAddress;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public Integer getCityId() { return cityId; }
        public void setCityId(Integer cityId) { this.cityId = cityId; }

        public String getCityName() { return cityName; }
        public void setCityName(String cityName) { this.cityName = cityName; }

        public Integer getDistrictId() { return districtId; }
        public void setDistrictId(Integer districtId) { this.districtId = districtId; }

        public String getDistrictName() { return districtName; }
        public void setDistrictName(String districtName) { this.districtName = districtName; }

        public Integer getWardId() { return wardId; }
        public void setWardId(Integer wardId) { this.wardId = wardId; }

        public String getWardName() { return wardName; }
        public void setWardName(String wardName) { this.wardName = wardName; }

        public Integer getCountryId() { return countryId; }
        public void setCountryId(Integer countryId) { this.countryId = countryId; }

        public String getCountryName() { return countryName; }
        public void setCountryName(String countryName) { this.countryName = countryName; }

        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

        public String getFullAddress() { return fullAddress; }
        public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    }
}