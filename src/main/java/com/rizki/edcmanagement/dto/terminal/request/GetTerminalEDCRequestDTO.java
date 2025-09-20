package com.rizki.edcmanagement.dto.terminal.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetTerminalEDCRequestDTO {
    // Pagination parameters
    @Min(value = 0, message = "Page number must be 0 or greater")
    @Builder.Default
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private Integer size = 10;

    // Sorting parameters
    @Builder.Default
    private String sortBy = "createdAt";
    @Builder.Default
    private String sortDirection = "desc"; // asc or desc

    // Filter parameters
    private String status; // Filter by status (ACTIVE, INACTIVE, etc.)
    private String location; // Filter by location (contains search)
    private String manufacturer; // Filter by manufacturer (contains search)
    private String model; // Filter by model (contains search)
    private String terminalType; // Filter by terminal type (EDC, ATM, POS, KIOSK)
    private String ipAddress; // Filter by IP address (exact match)
    private String serialNumber; // Filter by serial number (contains search)

    // Date range filters
    private String createdFrom; // Filter by created date from (YYYY-MM-DD)
    private String createdTo; // Filter by created date to (YYYY-MM-DD)
    private String lastMaintenanceFrom; // Filter by last maintenance from (YYYY-MM-DD)
    private String lastMaintenanceTo; // Filter by last maintenance to (YYYY-MM-DD)
}