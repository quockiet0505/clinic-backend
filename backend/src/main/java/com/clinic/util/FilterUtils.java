// com.clinic.util.FilterUtils.java
package com.clinic.util;

import com.clinic.dto.common.BaseFilterRequest;
import com.clinic.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilterUtils {

    public static Pageable buildPageable(BaseFilterRequest filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "createdAt";
        String sortDir = filter.getSortDir() != null ? filter.getSortDir() : "DESC";
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> PageResponse<T> buildPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .build();
    }
}