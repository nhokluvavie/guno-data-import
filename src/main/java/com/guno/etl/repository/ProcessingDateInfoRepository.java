// ProcessingDateInfoRepository.java - Processing Date Info Repository
package com.guno.etl.repository;

import com.guno.etl.entity.ProcessingDateInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessingDateInfoRepository extends JpaRepository<ProcessingDateInfo, String> {

    /**
     * Find orders processed on specific date
     */
    @Query("SELECT pdi FROM ProcessingDateInfo pdi WHERE DATE(pdi.fullDate) = DATE(:date)")
    List<ProcessingDateInfo> findByDate(@Param("date") LocalDateTime date);

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
     * Find orders processed in date range
     */
    List<ProcessingDateInfo> findByFullDateBetween(LocalDateTime startDate, LocalDateTime endDate);

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
    @Query("SELECT pdi.monthName, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.monthName, pdi.monthOfYear ORDER BY pdi.monthOfYear")
    List<Object[]> countOrdersByMonth();

    /**
     * Count orders by quarter
     */
    @Query("SELECT pdi.quarterName, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.quarterName, pdi.quarterOfYear ORDER BY pdi.quarterOfYear")
    List<Object[]> countOrdersByQuarter();

    /**
     * Count business day vs weekend orders
     */
    @Query("SELECT pdi.isBusinessDay, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.isBusinessDay")
    List<Object[]> countOrdersByBusinessDay();

    /**
     * Count holiday vs non-holiday orders
     */
    @Query("SELECT pdi.isHoliday, COUNT(pdi) FROM ProcessingDateInfo pdi GROUP BY pdi.isHoliday")
    List<Object[]> countOrdersByHoliday();

    /**
     * Find peak processing days
     */
    @Query("SELECT DATE(pdi.fullDate), COUNT(pdi) as orderCount FROM ProcessingDateInfo pdi " +
            "GROUP BY DATE(pdi.fullDate) ORDER BY orderCount DESC LIMIT 10")
    List<Object[]> findPeakProcessingDays();

    /**
     * Find orders in specific fiscal year and quarter
     */
    List<ProcessingDateInfo> findByFiscalYearAndFiscalQuarter(Integer fiscalYear, Integer fiscalQuarter);
}