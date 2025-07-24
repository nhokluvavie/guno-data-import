package com.guno.etl.repository;

import com.guno.etl.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Find orders by customer ID
     */
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Find orders by shop ID
     */
    List<Order> findByShopId(String shopId);

    /**
     * Count total orders for a customer
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    Integer countOrdersByCustomerId(@Param("customerId") String customerId);

    /**
     * Get customer's latest order date
     */
    @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.customerId = :customerId")
    Optional<LocalDateTime> findLatestOrderDateByCustomerId(@Param("customerId") String customerId);

    /**
     * Get customer's first order date
     */
    @Query("SELECT MIN(o.createdAt) FROM Order o WHERE o.customerId = :customerId")
    Optional<LocalDateTime> findFirstOrderDateByCustomerId(@Param("customerId") String customerId);

    /**
     * Calculate total spent by customer
     */
    @Query("SELECT COALESCE(SUM(o.grossRevenue), 0) FROM Order o WHERE o.customerId = :customerId")
    Double calculateTotalSpentByCustomerId(@Param("customerId") String customerId);


    /**
     * Count orders that have valid customer relationships
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE EXISTS (SELECT 1 FROM Customer c WHERE c.customerId = o.customerId)")
    Long countOrdersWithValidCustomers();

    /**
     * Find orders created within date range
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
