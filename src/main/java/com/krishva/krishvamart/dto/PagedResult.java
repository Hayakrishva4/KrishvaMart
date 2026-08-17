package com.krishva.krishvamart.dto;

import java.util.Collections;
import java.util.List;

public final class PagedResult<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final long totalItems;

    public PagedResult(List<T> items, int page, int pageSize, long totalItems) {
        this.items = items == null ? List.of() : List.copyOf(items);
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items == null ? List.of() : Collections.unmodifiableList(items);
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getTotalPages() {
        return pageSize <= 0 ? 0 : (long) Math.ceil((double) totalItems / pageSize);
    }
}