package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class ShopeeOrderDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("partner_id")
    private Long partnerId;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("total_amount")
    private Long totalAmount;

    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("update_time")
    private Long updateTime;

    @JsonProperty("data")
    private ShopeeOrderData data;

    @Data
    public static class ShopeeOrderData {

        @JsonProperty("cod")
        private Boolean cod;

        @JsonProperty("note")
        private String note;

        @JsonProperty("region")
        private String region;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("order_sn")
        private String orderSn;

        @JsonProperty("pay_time")
        private Long payTime;

        @JsonProperty("order_status")
        private String orderStatus;

        @JsonProperty("total_amount")
        private Long totalAmount;

        @JsonProperty("payment_method")
        private String paymentMethod;

        @JsonProperty("shipping_carrier")
        private String shippingCarrier;

        @JsonProperty("actual_shipping_fee")
        private Long actualShippingFee;

        @JsonProperty("estimated_shipping_fee")
        private Long estimatedShippingFee;

        @JsonProperty("order_chargeable_weight_gram")
        private Integer orderChargeableWeightGram;

        @JsonProperty("days_to_ship")
        private Integer daysToShip;

        @JsonProperty("item_list")
        private List<ShopeeItemDto> itemList;

        @JsonProperty("recipient_address")
        private ShopeeRecipientAddress recipientAddress;

        @JsonProperty("package_list")
        private List<ShopeePackageInfo> packageList;
    }

    @Data
    public static class ShopeeRecipientAddress {

        @JsonProperty("city")
        private String city;

        @JsonProperty("name")
        private String name;

        @JsonProperty("town")
        private String town;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("state")
        private String state;

        @JsonProperty("region")
        private String region;

        @JsonProperty("zipcode")
        private String zipcode;

        @JsonProperty("district")
        private String district;

        @JsonProperty("full_address")
        private String fullAddress;
    }

    @Data
    public static class ShopeePackageInfo {

        @JsonProperty("package_number")
        private String packageNumber;

        @JsonProperty("logistics_status")
        private String logisticsStatus;

        @JsonProperty("shipping_carrier")
        private String shippingCarrier;

        @JsonProperty("logistics_channel_id")
        private Long logisticsChannelId;

        @JsonProperty("parcel_chargeable_weight_gram")
        private Integer parcelChargeableWeightGram;
    }
}