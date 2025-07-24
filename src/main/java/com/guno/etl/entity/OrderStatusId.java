// OrderStatusId.java - Composite Key for OrderStatus
package com.guno.etl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusId implements Serializable {

    private Long statusKey;
    private String orderId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusId that = (OrderStatusId) o;
        return Objects.equals(statusKey, that.statusKey) &&
                Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusKey, orderId);
    }
}