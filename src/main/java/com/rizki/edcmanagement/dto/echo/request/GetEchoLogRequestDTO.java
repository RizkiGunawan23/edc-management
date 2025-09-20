package com.rizki.edcmanagement.dto.echo.request;

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
public class GetEchoLogRequestDTO {
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
    private String sortBy = "timestamp";
    @Builder.Default
    private String sortDirection = "desc"; // asc or desc

    // Filter parameters
    private String terminalId; // Filter by terminal ID (contains search)

    // Date range filters
    private String timestampFrom; // Filter by timestamp from (YYYY-MM-DD)
    private String timestampTo; // Filter by timestamp to (YYYY-MM-DD)
}