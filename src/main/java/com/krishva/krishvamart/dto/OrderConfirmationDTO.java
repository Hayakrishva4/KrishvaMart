package com.krishva.krishvamart.dto;

import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.OrderItem;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class OrderConfirmationDTO {
    private final Long orderId;
    private final String status;
    private final BigDecimal totalAmount;
    private final String shippingAddress;
    private final List<OrderItem> items;
    private final String message;

    private OrderConfirmationDTO(Builder builder) {
        this.orderId = builder.orderId;
        this.status = builder.status;
        this.totalAmount = builder.totalAmount;
        this.shippingAddress = builder.shippingAddress;
        this.items = builder.items == null ? List.of() : List.copyOf(builder.items);
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static OrderConfirmationDTO fromOrder(Order order) {
        return builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(order.getItems())
                .message("Order #" + order.getId() + " placed successfully. Mock payment confirmed.")
                .build();
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStatus() { 
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItem> getItems() {
        return items == null ? List.of() : Collections.unmodifiableList(items);
    }

    public String getMessage() {
        return message;
    }

    public static final class Builder {
        private Long orderId;
        private String status;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private List<OrderItem> items;
        private String message;

        private Builder() {}

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder shippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items == null ? List.of() : List.copyOf(items);
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public OrderConfirmationDTO build() {
            if (orderId == null) {
                throw new IllegalStateException("orderId is required");
            }
            if (status == null) {
                throw new IllegalStateException("status is required");
            }
            return new OrderConfirmationDTO(this);
        }
    }
}