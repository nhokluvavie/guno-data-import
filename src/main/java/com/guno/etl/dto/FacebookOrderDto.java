// FacebookOrderDto.java - OPTIMIZED with String IDs
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookOrderDto {

    @JsonProperty("id")
    private String id;  // ✅ Fixed: String for large IDs

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status")
    private Integer status;  // Small status codes: 1,2,3,9

    @JsonProperty("data")
    private FacebookOrderData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookOrderData {

        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

        @JsonProperty("cod")
        private Integer cod;

        @JsonProperty("tax")
        private Integer tax;

        @JsonProperty("cash")
        private Integer cash;

        @JsonProperty("link")
        private String link;

        @JsonProperty("note")
        private String note;

        @JsonProperty("page")
        private FacebookPageDto page;

        @JsonProperty("tags")
        private List<FacebookTagDto> tags;

        @JsonProperty("type")
        private String type;

        @JsonProperty("ad_id")
        private String adId;

        @JsonProperty("items")
        private List<FacebookItemDto> items;

        @JsonProperty("customer")
        private FacebookCustomerDto customer;

        @JsonProperty("history")
        private List<FacebookHistoryDto> history;

        @JsonProperty("pke_mkter")
        private String pkeMkter;

        @JsonProperty("surcharge")
        private Integer surcharge;

        @JsonProperty("system_id")
        private String systemId;  // ✅ Fixed: String for large IDs

        @JsonProperty("ads_source")
        private String adsSource;

        @JsonProperty("bill_email")
        private String billEmail;

        @JsonProperty("event_type")
        private String eventType;

        @JsonProperty("note_image")
        private String noteImage;

        @JsonProperty("note_print")
        private String notePrint;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("inserted_at")
        private String insertedAt;

        @JsonProperty("time_close")
        private String timeClose;

        @JsonProperty("time_confirm")
        private String timeConfirm;

        @JsonProperty("order_source")
        private String orderSource;

        @JsonProperty("partner_fee")
        private Integer partnerFee;

        @JsonProperty("customer_pay_fee")
        private Boolean customerPayFee;

        @JsonProperty("extend_code")
        private String extendCode;  // ✅ Fixed: String for large codes

        @JsonProperty("partner_id")
        private String partnerId;  // ✅ Fixed: String for large IDs

        @JsonProperty("shop_partner_id")
        private String shopPartnerId;  // ✅ Fixed: String for large IDs

        @JsonProperty("time_send_partner")
        private String timeSendPartner;

        @JsonProperty("user_send_partner")
        private String userSendPartner;

        @JsonProperty("printed_form")
        private String printedForm;

        @JsonProperty("shipping_address")
        private FacebookShippingAddressDto shippingAddress;

        @JsonProperty("shipping_fee")
        private Double shippingFee;
    }

    // ===== NESTED DTOs =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookTagDto {
        @JsonProperty("id")
        private String id;  // ✅ Fixed: String for large IDs

        @JsonProperty("name")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookHistoryDto {
        @JsonProperty("tags")
        private FacebookTagChangeDto tags;

        @JsonProperty("status")
        private FacebookStatusChangeDto status;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookTagChangeDto {
        @JsonProperty("added")
        private List<FacebookTagDto> added;

        @JsonProperty("removed")
        private List<FacebookTagDto> removed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookStatusChangeDto {
        @JsonProperty("from")
        private Integer from;

        @JsonProperty("to")
        private Integer to;

        @JsonProperty("reason")
        private String reason;
    }
}