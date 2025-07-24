// OrderStatusRepository.java - Order Status Repository
package com.guno.etl.repository;

import com.guno.etl.entity.OrderStatus;
import com.guno.etl.entity.OrderStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, OrderStatusId> {

    /**
     * Find all status transitions for an order
     */
    List<OrderStatus> findByOrderIdOrderByTransitionTimestamp(String orderId);

    /**
     * Find current status for an order (latest transition)
     */
    @Query("SELECT os FROM OrderStatus os WHERE os.orderId = :orderId ORDER BY os.transitionTimestamp DESC LIMIT 1")
    Optional<OrderStatus> findCurrentStatusByOrderId(@Param("orderId") String orderId);

    /**
     * Find orders by status key
     */
    List<OrderStatus> findByStatusKeyOrderByTransitionTimestamp(Long statusKey);

    /**
     * Find status transitions within date range
     */
    List<OrderStatus> findByTransitionTimestampBetweenOrderByTransitionTimestamp(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders that transitioned to specific status on specific date
     */
    @Query("SELECT os FROM OrderStatus os WHERE os.statusKey = :statusKey " +
            "AND DATE(os.transitionTimestamp) = DATE(:date)")
    List<OrderStatus> findByStatusKeyAndTransitionDate(@Param("statusKey") Long statusKey,
                                                       @Param("date") LocalDateTime date);

    /**
     * Count status transitions for an order
     */
    int countByOrderId(String orderId);

    /**
     * Find orders with specific transition trigger
     */
    List<OrderStatus> findByTransitionTrigger(String transitionTrigger);

    /**
     * Find orders changed by specific user
     */
    List<OrderStatus> findByChangedBy(String changedBy);

    /**
     * Check if order has specific status
     */
    boolean existsByOrderIdAndStatusKey(String orderId, Long statusKey);

    /**
     * Get next history key for new transitions
     */
    @Query("SELECT COALESCE(MAX(os.historyKey), 0) + 1 FROM OrderStatus os")
    Long findNextHistoryKey();

    /**
     * Find delayed transitions (longer than expected)
     */
    @Query("SELECT os FROM OrderStatus os WHERE os.isOnTimeTransition = false " +
            "AND os.transitionTimestamp >= :startDate")
    List<OrderStatus> findDelayedTransitions(@Param("startDate") LocalDateTime startDate);

    // Thêm vào OrderStatusRepository.java (sau các method hiện có)

    /**
     * Find order status by composite key
     */
    Optional<OrderStatus> findByStatusKeyAndOrderId(Long statusKey, String orderId);

    /**
     * Find order transitions by order ID, ordered by timestamp descending
     */
    List<OrderStatus> findByOrderIdOrderByTransitionTimestampDesc(String orderId);
}
