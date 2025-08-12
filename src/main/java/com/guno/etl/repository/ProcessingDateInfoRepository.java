package com.guno.etl.repository;

import com.guno.etl.entity.ProcessingDateInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessingDateInfoRepository extends JpaRepository<ProcessingDateInfo, String> {

    // ===== FIXED: Remove problematic methods that cause type mismatch =====

    /**
     * Find orders processed in specific year
     */
    List<ProcessingDateInfo> findByYear(Integer year);

    /**
     * Find orders processed in specific month
     */
    List<ProcessingDateInfo> findByYearAndMonthOfYear(Integer year, Integer month);

    /**
     * Find orders processed in specific quarter
     */
    List<ProcessingDateInfo> findByYearAndQuarterOfYear(Integer year, Integer quarter);

    /**
     * Find orders processed on weekends
     */
    List<ProcessingDateInfo> findByIsWeekendTrue();

    /**
     * Find orders processed on holidays
     */
    List<ProcessingDateInfo> findByIsHolidayTrue();

    /**
     * Find orders processed on business days
     */
    List<ProcessingDateInfo> findByIsBusinessDayTrue();

    /**
     * Find orders processed during shopping seasons
     */
    List<ProcessingDateInfo> findByIsShoppingSeasonTrue();

    /**
     * Find orders processed during peak hours
     */
    List<ProcessingDateInfo> findByIsPeakHourTrue();

    /**
     * Find orders by day of week
     */
    List<ProcessingDateInfo> findByDayOfWeek(Integer dayOfWeek);

    /**
     * Find orders by day of week name
     */
    List<ProcessingDateInfo> findByDayOfWeekName(String dayOfWeekName);

    /**
     * Find orders by month name
     */
    List<ProcessingDateInfo> findByMonthName(String monthName);

    /**
     * Find orders by season
     */
    List<ProcessingDateInfo> findBySeasonName(String seasonName);

    /**
     * Get next date key for auto-generation
     */
    @Query("SELECT COALESCE(MAX(pdi.dateKey), 0) + 1 FROM ProcessingDateInfo pdi")
    Long findNextDateKey();

    /**
     * Count orders by day of week
     */
    @Query("SELECT pdi.dayOfWeekName, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.dayOfWeekName, pdi.dayOfWeek ORDER BY pdi.dayOfWeek")
    List<Object[]> countOrdersByDayOfWeek();

    /**
     * Count orders by month
     */
    @Query("SELECT pdi.monthName, COUNT(pdi) FROM ProcessingDateInfo pdi WHERE pdi.year = :year GROUP BY pdi.monthName, pdi.monthOfYear ORDER BY pdi.monthOfYear")
    List<Object[]> countOrdersByMonth(@Param("year") Integer year);

    /**
     * Count orders by quarter
     */
    @Query("SELECT pdi.quarterName, COUNT(pdi) FROM ProcessingDateInfo pdi WHERE pdi.year = :year GROUP BY pdi.quarterName, pdi.quarterOfYear ORDER BY pdi.quarterOfYear")
    List<Object[]> countOrdersByQuarter(@Param("year") Integer year);

    /**
     * Count business day vs weekend orders
     */
    @Query("SELECT pdi.isWeekend, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.isWeekend")
    List<Object[]> countOrdersByWeekendStatus();

    /**
     * Count holiday vs non-holiday orders
     */
    @Query("SELECT pdi.isHoliday, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.isHoliday")
    List<Object[]> countOrdersByHolidayStatus();

    /**
     * Check if order exists
     */
    boolean existsByOrderId(String orderId);
}