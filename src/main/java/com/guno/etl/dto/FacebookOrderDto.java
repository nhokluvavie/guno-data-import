// FacebookOrderDto.java - Facebook Order DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

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

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

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

        @JsonProperty("total")
        private Long total;

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

        @JsonProperty("shipping_address")
        private FacebookShippingAddress shippingAddress;

        @JsonProperty("payment_method")
        private String paymentMethod;

        @JsonProperty("delivery_status")
        private String deliveryStatus;

        @JsonProperty("order_source")
        private String orderSource;

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

        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }

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

        public FacebookShippingAddress getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(FacebookShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getDeliveryStatus() { return deliveryStatus; }
        public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

        public String getOrderSource() { return orderSource; }
        public void setOrderSource(String orderSource) { this.orderSource = orderSource; }
    }

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

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<Object> getTags() { return tags; }
        public void setTags(List<Object> tags) { this.tags = tags; }

        public String getFbId() { return fbId; }
        public void setFbId(String fbId) { this.fbId = fbId; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public List<Object> getNotes() { return notes; }
        public void setNotes(List<Object> notes) { this.notes = notes; }

        public List<String> getEmails() { return emails; }
        public void setEmails(List<String> emails) { this.emails = emails; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }

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

        public List<Object> getListVoucher() { return listVoucher; }
        public void setListVoucher(List<Object> listVoucher) { this.listVoucher = listVoucher; }

        public Integer getRewardPoint() { return rewardPoint; }
        public void setRewardPoint(Integer rewardPoint) { this.rewardPoint = rewardPoint; }

        public Long getCurrentDebts() { return currentDebts; }
        public void setCurrentDebts(Long currentDebts) { this.currentDebts = currentDebts; }

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

        public Long getPurchasedAmount() { return purchasedAmount; }
        public void setPurchasedAmount(Long purchasedAmount) { this.purchasedAmount = purchasedAmount; }

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

        public Long getTotalAmountReferred() { return totalAmountReferred; }
        public void setTotalAmountReferred(Long totalAmountReferred) { this.totalAmountReferred = totalAmountReferred; }

        public List<FacebookShippingAddress> getShopCustomerAddresses() { return shopCustomerAddresses; }
        public void setShopCustomerAddresses(List<FacebookShippingAddress> shopCustomerAddresses) { this.shopCustomerAddresses = shopCustomerAddresses; }
    }

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