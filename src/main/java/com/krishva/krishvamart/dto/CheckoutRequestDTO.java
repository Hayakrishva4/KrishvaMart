package com.krishva.krishvamart.dto;

public class CheckoutRequestDTO {
    private String shippingAddress;
    private Long directProductId;
    private Integer directQuantity;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Long getDirectProductId() {
        return directProductId;
    }

    public void setDirectProductId(Long directProductId) {
        this.directProductId = directProductId;
    }

    public Integer getDirectQuantity() {
        return directQuantity;
    }

    public void setDirectQuantity(Integer directQuantity) {
        this.directQuantity = directQuantity;
    }
}