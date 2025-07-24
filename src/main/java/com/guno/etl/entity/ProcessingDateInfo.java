// ProcessingDateInfo.java - Processing Date Info Entity
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_processing_date_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingDateInfo {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @Column(name = "date_key")
    private Long dateKey;

    @Column(name = "full_date")
    private LocalDateTime fullDate;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "day_of_week_name")
    private String dayOfWeekName;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "day_of_year")
    private Integer dayOfYear;

    @Column(name = "week_of_year")
    private Integer weekOfYear;

    @Column(name = "month_of_year")
    private Integer monthOfYear;

    @Column(name = "month_name")
    private String monthName;

    @Column(name = "quarter_of_year")
    private Integer quarterOfYear;

    @Column(name = "quarter_name")
    private String quarterName;

    @Column(name = "year")
    private Integer year;

    @Column(name = "is_weekend")
    private Boolean isWeekend;

    @Column(name = "is_holiday")
    private Boolean isHoliday;

    @Column(name = "holiday_name")
    private String holidayName;

    @Column(name = "is_business_day")
    private Boolean isBusinessDay;

    @Column(name = "fiscal_year")
    private Integer fiscalYear;

    @Column(name = "fiscal_quarter")
    private Integer fiscalQuarter;

    @Column(name = "is_shopping_season")
    private Boolean isShoppingSeason;

    @Column(name = "season_name")
    private String seasonName;

    @Column(name = "is_peak_hour")
    private Boolean isPeakHour;
}