package com.guno.etl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductId implements Serializable {

    private String sku;
    private String platformProductId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductId productId = (ProductId) o;

        if (!sku.equals(productId.sku)) return false;
        return platformProductId.equals(productId.platformProductId);
    }

    @Override
    public int hashCode() {
        int result = sku.hashCode();
        result = 31 * result + platformProductId.hashCode();
        return result;
    }
}