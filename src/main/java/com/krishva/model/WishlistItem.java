package com.krishva.krishvamart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** O1: a product a buyer has saved for later, outside their active cart. */
public class WishlistItem {

    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String productImageUrl;
    private int productStockQty;
    private LocalDateTime createdAt;

    public WishlistItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public int getProductStockQty() {
        return productStockQty;
    }

    public void setProductStockQty(int productStockQty) {
        this.productStockQty = productStockQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
