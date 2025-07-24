// Status.java - Status Entity
package com.guno.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_key")
    private Long statusKey;

    @Column(name = "platform")
    private String platform;

    @Column(name = "platform_status_code")
    private String platformStatusCode;

    @Column(name = "platform_status_name")
    private String platformStatusName;

    @Column(name = "standard_status_code")
    private String standardStatusCode;

    @Column(name = "standard_status_name")
    private String standardStatusName;

    @Column(name = "status_category")
    private String statusCategory;
}