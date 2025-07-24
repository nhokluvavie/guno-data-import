// PaymentInfoRepository.java - Payment Info Repository
package com.guno.etl.repository;

import com.guno.etl.entity.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, String> {

    /**
     * Find payment info by payment method
     */
    List<PaymentInfo> findByPaymentMethod(String paymentMethod);

    /**
     * Find payment info by payment category
     */
    List<PaymentInfo> findByPaymentCategory(String paymentCategory);

    /**
     * Find payment info by payment provider
     */
    List<PaymentInfo> findByPaymentProvider(String paymentProvider);

    /**
     * Find COD orders
     */
    List<PaymentInfo> findByIsCodTrue();

    /**
     * Find prepaid orders
     */
    List<PaymentInfo> findByIsPrepaidTrue();

    /**
     * Find installment orders
     */
    List<PaymentInfo> findByIsInstallmentTrue();

    /**
     * Find orders supporting refund
     */
    List<PaymentInfo> findBySupportsRefundTrue();

    /**
     * Find orders requiring verification
     */
    List<PaymentInfo> findByRequiresVerificationTrue();

    /**
     * Find payment info by risk level
     */
    List<PaymentInfo> findByRiskLevel(String riskLevel);

    /**
     * Find orders by fraud score range
     */
    @Query("SELECT pi FROM PaymentInfo pi WHERE pi.fraudScore BETWEEN :minScore AND :maxScore")
    List<PaymentInfo> findByFraudScoreRange(@Param("minScore") Double minScore,
                                            @Param("maxScore") Double maxScore);

    /**
     * Find orders by transaction fee rate range
     */
    @Query("SELECT pi FROM PaymentInfo pi WHERE pi.transactionFeeRate BETWEEN :minRate AND :maxRate")
    List<PaymentInfo> findByTransactionFeeRateRange(@Param("minRate") Double minRate,
                                                    @Param("maxRate") Double maxRate);

    /**
     * Find orders by processing time range
     */
    @Query("SELECT pi FROM PaymentInfo pi WHERE pi.paymentProcessingTimeMinutes BETWEEN :minMinutes AND :maxMinutes")
    List<PaymentInfo> findByProcessingTimeRange(@Param("minMinutes") Integer minMinutes,
                                                @Param("maxMinutes") Integer maxMinutes);

    /**
     * Get next payment key for auto-generation
     */
    @Query("SELECT COALESCE(MAX(pi.paymentKey), 0) + 1 FROM PaymentInfo pi")
    Long findNextPaymentKey();

    /**
     * Count orders by payment method
     */
    @Query("SELECT pi.paymentMethod, COUNT(pi) FROM PaymentInfo pi GROUP BY pi.paymentMethod")
    List<Object[]> countOrdersByPaymentMethod();

    /**
     * Count COD vs Non-COD orders
     */
    @Query("SELECT pi.isCod, COUNT(pi) FROM PaymentInfo pi GROUP BY pi.isCod")
    List<Object[]> countOrdersByCodStatus();

    /**
     * Find high-risk transactions
     */
    @Query("SELECT pi FROM PaymentInfo pi WHERE pi.fraudScore > :threshold OR pi.riskLevel = 'HIGH'")
    List<PaymentInfo> findHighRiskTransactions(@Param("threshold") Double threshold);
}