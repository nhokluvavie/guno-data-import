package com.guno.etl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemId implements Serializable {

    private String orderId;
    private String sku;
    private String platformProductId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderItemId that = (OrderItemId) o;

        if (!orderId.equals(that.orderId)) return false;
        if (!sku.equals(that.sku)) return false;
        return platformProductId.equals(that.platformProductId);
    }

    @Override
    public int hashCode() {
        int result = orderId.hashCode();
        result = 31 * result + sku.hashCode();
        result = 31 * result + platformProductId.hashCode();
        return result;
    }
}