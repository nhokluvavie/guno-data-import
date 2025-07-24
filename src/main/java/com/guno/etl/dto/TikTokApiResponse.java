// TikTokApiResponse.java - TikTok API Response DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TikTokApiResponse {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("data")
    private TikTokDataWrapper data;

    // Getters and setters
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public TikTokDataWrapper getData() { return data; }
    public void setData(TikTokDataWrapper data) { this.data = data; }

    public static class TikTokDataWrapper {

        @JsonProperty("orders")
        private List<TikTokOrderDto> orders;

        @JsonProperty("count")
        private Integer count;

        @JsonProperty("page")
        private Integer page;

        // Getters and setters
        public List<TikTokOrderDto> getOrders() { return orders; }
        public void setOrders(List<TikTokOrderDto> orders) { this.orders = orders; }

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }

        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
    }
}