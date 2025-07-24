package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShopeeItemDto {

    @JsonProperty("weight")
    private Double weight;

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("item_sku")
    private String itemSku;

    @JsonProperty("model_id")
    private Long modelId;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("model_sku")
    private String modelSku;

    @JsonProperty("wholesale")
    private Boolean wholesale;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("add_on_deal")
    private Boolean addOnDeal;

    @JsonProperty("promotion_id")
    private Long promotionId;

    @JsonProperty("order_item_id")
    private Long orderItemId;

    @JsonProperty("add_on_deal_id")
    private Long addOnDealId;

    @JsonProperty("promotion_type")
    private String promotionType;

    @JsonProperty("hot_listing_item")
    private Boolean hotListingItem;

    @JsonProperty("is_b2c_owned_item")
    private Boolean isB2cOwnedItem;

    @JsonProperty("promotion_group_id")
    private Long promotionGroupId;

    @JsonProperty("is_prescription_item")
    private Boolean isPrescriptionItem;

    @JsonProperty("model_original_price")
    private Long modelOriginalPrice;

    @JsonProperty("model_discounted_price")
    private Long modelDiscountedPrice;

    @JsonProperty("model_quantity_purchased")
    private Integer modelQuantityPurchased;

    @JsonProperty("image_info")
    private ShopeeImageInfo imageInfo;

    @Data
    public static class ShopeeImageInfo {

        @JsonProperty("image_url")
        private String imageUrl;
    }
}