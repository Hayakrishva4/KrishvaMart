package com.krishva.krishvamart.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public final class SellerSalesSummaryDTO {

    private final long totalOrders;
    private final long totalUnitsSold;
    private final BigDecimal totalRevenue;
    private final List<ProductSalesDTO> byProduct;

    private SellerSalesSummaryDTO(Builder b) {
        this.totalOrders = b.totalOrders;
        this.totalUnitsSold = b.totalUnitsSold;
        this.totalRevenue = b.totalRevenue;
     this.byProduct = b.byProduct == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(b.byProduct));
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public List<ProductSalesDTO> getByProduct() {
     return byProduct == null ? Collections.emptyList()
         : Collections.unmodifiableList(byProduct);
        }

    public static final class Builder {
        private long totalOrders;
        private long totalUnitsSold;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private List<ProductSalesDTO> byProduct;

        private Builder() {
        }

        public Builder totalOrders(long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public Builder totalUnitsSold(long totalUnitsSold) {
            this.totalUnitsSold = totalUnitsSold;
            return this;
        }

        public Builder totalRevenue(BigDecimal totalRevenue) {
         this.totalRevenue = totalRevenue == null ? BigDecimal.ZERO : totalRevenue;
            return this;
        }

        public Builder byProduct(List<ProductSalesDTO> byProduct) {
         this.byProduct = byProduct == null ? null : new ArrayList<>(byProduct);
            return this;
        }

        public SellerSalesSummaryDTO build() {
            return new SellerSalesSummaryDTO(this);
        }
    }
}