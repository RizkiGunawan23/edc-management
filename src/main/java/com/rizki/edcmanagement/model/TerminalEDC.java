package com.rizki.edcmanagement.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.rizki.edcmanagement.model.enums.TerminalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "terminal_edc", indexes = {
        @Index(name = "idx_terminal_type", columnList = "terminalId"),
        @Index(name = "idx_terminal_status", columnList = "status"),
        @Index(name = "idx_serial_number", columnList = "serialNumber"),
        @Index(name = "idx_terminal_location", columnList = "location"),
        @Index(name = "idx_last_maintenance", columnList = "lastMaintenance"),
        @Index(name = "idx_terminal_ip", columnList = "ipAddress"),
        @Index(name = "idx_created_at", columnList = "createdAt"),
})
@EntityListeners(AuditingEntityListener.class)
public class TerminalEDC {
    /**
     * Terminal ID following pattern: {TYPE}-{LOCATION}-{SEQUENCE}
     * Examples: EDC-JKT-001, ATM-BDG-045, POS-SBY-123
     */
    @Id
    @Column(nullable = false, length = 15)
    private String terminalId;

    @Column(nullable = false)
    private String location;

    /**
     * Current operational status of the terminal
     * Stored as enum in database for data integrity
     * Default value: INACTIVE for new terminals
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'INACTIVE'")
    @Builder.Default
    private TerminalStatus status = TerminalStatus.INACTIVE;

    private String serialNumber;

    private String model;

    private String manufacturer;

    private LocalDateTime lastMaintenance;

    private String ipAddress;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}