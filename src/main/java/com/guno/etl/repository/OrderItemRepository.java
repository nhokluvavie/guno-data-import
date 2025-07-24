package com.guno.etl.repository;

import com.guno.etl.entity.OrderItem;
import com.guno.etl.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    /**
     * Find all items for a specific order
     */
    List<OrderItem> findByOrderIdOrderByItemSequence(String orderId);

    /**
     * Find all orders containing a specific product
     */
    List<OrderItem> findBySkuAndPlatformProductId(String sku, String platformProductId);

    /**
     * Count total quantity for a product across all orders
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.sku = :sku AND oi.platformProductId = :platformProductId")
    Integer countTotalQuantityBySku(@Param("sku") String sku, @Param("platformProductId") String platformProductId);

    /**
     * Calculate total revenue for a product
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.sku = :sku AND oi.platformProductId = :platformProductId")
    Double calculateTotalRevenueByProduct(@Param("sku") String sku, @Param("platformProductId") String platformProductId);

    /**
     * Find items with promotions
     */
    List<OrderItem> findByPromotionTypeIsNotNull();
}