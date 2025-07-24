// OrderStatusDetailRepository.java - Order Status Detail Repository
package com.guno.etl.repository;

import com.guno.etl.entity.OrderStatusDetail;
import com.guno.etl.entity.OrderStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusDetailRepository extends JpaRepository<OrderStatusDetail, OrderStatusId> {

    /**
     * Find status detail by order ID
     */
    Optional<OrderStatusDetail> findByOrderId(String orderId);

    /**
     * Find status details by status key
     */
    List<OrderStatusDetail> findByStatusKey(Long statusKey);

    /**
     * Find active orders
     */
    List<OrderStatusDetail> findByIsActiveOrderTrue();

    /**
     * Find completed orders
     */
    List<OrderStatusDetail> findByIsCompletedOrderTrue();

    /**
     * Find revenue recognized orders
     */
    List<OrderStatusDetail> findByIsRevenueRecognizedTrue();

    /**
     * Find refundable orders
     */
    List<OrderStatusDetail> findByIsRefundableTrue();

    /**
     * Find cancellable orders
     */
    List<OrderStatusDetail> findByIsCancellableTrue();

    /**
     * Find trackable orders
     */
    List<OrderStatusDetail> findByIsTrackableTrue();

    /**
     * Find orders requiring manual action
     */
    List<OrderStatusDetail> findByRequiresManualActionTrue();

    /**
     * Find customer visible statuses
     */
    List<OrderStatusDetail> findByCustomerVisibleTrue();

    /**
     * Find orders by status color
     */
    List<OrderStatusDetail> findByStatusColor(String statusColor);

    /**
     * Find orders with auto transition enabled
     */
    @Query("SELECT osd FROM OrderStatusDetail osd WHERE osd.autoTransitionHours > 0")
    List<OrderStatusDetail> findOrdersWithAutoTransition();

    /**
     * Find orders by success rate range
     */
    @Query("SELECT osd FROM OrderStatusDetail osd WHERE osd.successRate BETWEEN :minRate AND :maxRate")
    List<OrderStatusDetail> findBySuccessRateRange(@Param("minRate") Double minRate,
                                                   @Param("maxRate") Double maxRate);

    /**
     * Find orders by average duration range
     */
    @Query("SELECT osd FROM OrderStatusDetail osd WHERE osd.averageDurationHours BETWEEN :minHours AND :maxHours")
    List<OrderStatusDetail> findByAverageDurationRange(@Param("minHours") Double minHours,
                                                       @Param("maxHours") Double maxHours);

    /**
     * Count orders by status characteristics
     */
    @Query("SELECT COUNT(osd) FROM OrderStatusDetail osd WHERE " +
            "osd.isActiveOrder = :isActive AND osd.isCompletedOrder = :isCompleted")
    long countByStatusCharacteristics(@Param("isActive") Boolean isActive,
                                      @Param("isCompleted") Boolean isCompleted);

    /**
     * Find order status detail by composite key
     */
    Optional<OrderStatusDetail> findByStatusKeyAndOrderId(Long statusKey, String orderId);
}