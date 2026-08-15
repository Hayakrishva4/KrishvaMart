package com.krishva.krishvamart.dto;

import java.math.BigDecimal;

/**
 * Search/filter/sort/pagination criteria for F3 browse (extended beyond the
 * minimum keyword+category spec with a price range, sort order, and
 * pagination - the kind of browse experience real marketplaces offer).
 * Built via {@link Builder} since it has several optional fields.
 */
public final class ProductSearchCriteria {

    public enum SortBy {
        RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST
    }

    private final String keyword;
    private final String category;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final SortBy sortBy;
    private final int page;
    private final int pageSize;
    private final boolean activeOnly;

    private ProductSearchCriteria(Builder b) {
        this.keyword = b.keyword;
        this.category = b.category;
        this.minPrice = b.minPrice;
        this.maxPrice = b.maxPrice;
        this.sortBy = b.sortBy;
        this.page = b.page;
        this.pageSize = b.pageSize;
        this.activeOnly = b.activeOnly;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    /** Builder for {@link ProductSearchCriteria}; defaults to page 1, 12 per page, relevance order, active-only. */
    public static final class Builder {
        private String keyword;
        private String category;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private SortBy sortBy = SortBy.RELEVANCE;
        private int page = 1;
        private int pageSize = 12;
        private boolean activeOnly = true;

        private Builder() {
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder sortBy(SortBy sortBy) {
            this.sortBy = sortBy == null ? SortBy.RELEVANCE : sortBy;
            return this;
        }

        public Builder page(int page) {
            this.page = Math.max(page, 1);
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize < 1 || pageSize > 60 ? 12 : pageSize;
            return this;
        }

        public Builder activeOnly(boolean activeOnly) {
            this.activeOnly = activeOnly;
            return this;
        }

        public ProductSearchCriteria build() {
            return new ProductSearchCriteria(this);
        }
    }
}
