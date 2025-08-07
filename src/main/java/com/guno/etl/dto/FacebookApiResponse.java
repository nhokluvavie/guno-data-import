// FacebookApiResponse.java - OPTIMIZED
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookApiResponse {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("data")
    private FacebookData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacebookData {

        @JsonProperty("orders")
        private List<FacebookOrderDto> orders;

        @JsonProperty("total")
        private Integer total;

        @JsonProperty("page")
        private Integer page;

        @JsonProperty("limit")
        private Integer limit;
    }
}