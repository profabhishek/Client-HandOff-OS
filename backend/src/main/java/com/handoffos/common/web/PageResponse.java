package com.handoffos.common.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Standard shape for every list endpoint:
 * { "items": [...], "page": 0, "size": 25, "totalItems": 42 }
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }
}
