package com.krishva.krishvamart.dto;

/** Incoming JSON body for PATCH /api/v1/orders/{id}/status (O2). */
public class OrderStatusRequestDTO {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
