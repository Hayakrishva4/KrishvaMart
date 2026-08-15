package com.krishva.krishvamart.dto;

/** Incoming JSON body for POST/PUT /api/v1/cart/items (F4). */
public class CartItemRequestDTO {
    private Long productId;
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
