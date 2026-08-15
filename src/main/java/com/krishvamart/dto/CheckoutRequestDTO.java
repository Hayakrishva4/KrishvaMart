package com.krishva.krishvamart.dto;

/** Incoming JSON body for POST /api/v1/orders/checkout. */
public class CheckoutRequestDTO {
    private String shippingAddress;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
