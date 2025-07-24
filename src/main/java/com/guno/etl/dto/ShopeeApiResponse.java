package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ShopeeApiResponse {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("data")
    private ShopeeDataWrapper data;

    @Data
    public static class ShopeeDataWrapper {

        @JsonProperty("orders")
        private List<ShopeeOrderDto> orders;

        @JsonProperty("count")
        private Integer count;

        @JsonProperty("page")
        private Integer page;
    }
}