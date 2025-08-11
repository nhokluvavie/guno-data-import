package com.guno.etl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookShippingAddressDto {

    @JsonProperty("full_address")
    private String fullAddress;

    @JsonProperty("name")
    private String recipientName;

    @JsonProperty("phone")
    private String recipientPhone;

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

    @JsonProperty("address_type")
    private String addressType; // HOME, OFFICE, etc.

    @JsonProperty("is_default")
    private Boolean isDefault;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;
}