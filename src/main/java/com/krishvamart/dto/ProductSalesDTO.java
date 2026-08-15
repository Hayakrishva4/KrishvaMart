package com.krishva.krishvamart.dto;

import java.math.BigDecimal;

/** O3: per-product row in a seller's sales dashboard - units sold and revenue from DELIVERED orders. */
public class ProductSalesDTO {

    private final Long productId;
    private final String productName;
    private final long unitsSold;
    private final BigDecimal revenue;

    public ProductSalesDTO(Long productId, String productName, long unitsSold, BigDecimal revenue) {
        this.productId = productId;
        this.productName = productName;
        this.unitsSold = unitsSold;
        this.revenue = revenue;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public long getUnitsSold() {
        return unitsSold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
