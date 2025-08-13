// FacebookApiResponse.java - Facebook API Response DTO
package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FacebookApiResponse {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("data")
    private FacebookDataWrapper data;

    // Getters and setters
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public FacebookDataWrapper getData() { return data; }
    public void setData(FacebookDataWrapper data) { this.data = data; }

    public static class FacebookDataWrapper {

        @JsonProperty("orders")
        private List<FacebookOrderDto> orders;

        @JsonProperty("count")
        private Integer count;

        @JsonProperty("page")
        private Integer page;

        // Getters and setters
        public List<FacebookOrderDto> getOrders() { return orders; }
        public void setOrders(List<FacebookOrderDto> orders) { this.orders = orders; }

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }

        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
    }
}