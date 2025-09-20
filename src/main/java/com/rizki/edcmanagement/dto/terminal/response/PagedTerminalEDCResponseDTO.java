package com.rizki.edcmanagement.dto.terminal.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedTerminalEDCResponseDTO {
    private List<TerminalEDCResponseDTO> terminalEDC;

    // Pagination metadata
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    private int numberOfElements;

    // Sorting metadata
    private String sortBy;
    private String sortDirection;

    // Filter metadata (optional - to show what filters were applied)
    private String appliedFilters;
}