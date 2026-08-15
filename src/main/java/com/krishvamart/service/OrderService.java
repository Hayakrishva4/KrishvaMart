package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.model.CartItem;
import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.OrderItem;
import com.krishva.krishvamart.model.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final DataSource dataSource;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;

    public OrderService(DataSource dataSource, OrderDAO orderDAO, ProductDAO productDAO, CartDAO cartDAO) {
        this.dataSource = dataSource;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
    }

    public Order checkout(long buyerId, boolean paymentSuccess) {
        return checkout(buyerId, paymentSuccess, "Standard Shipping");
    }

    public Order checkout(long buyerId, boolean paymentSuccess, String shippingAddress) {
        List<CartItem> cartItems = cartDAO.findByUserId(buyerId);
        if (cartItems.isEmpty()) {
            throw AppException.badRequest("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productDAO.findById(item.getProductId())
                    .orElseThrow(() -> AppException.notFound("Product not found"));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw AppException.badRequest("Insufficient stock for product: " + product.getTitle());
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductTitle(product.getTitle());
            orderItem.setPriceAtPurchase(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setTotalAmount(total);
        order.setStatus(paymentSuccess ? Order.Status.CONFIRMED : Order.Status.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(orderItems);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Order created = orderDAO.create(order);

                if (paymentSuccess) {
                    for (CartItem item : cartItems) {
                        Product product = productDAO.findById(item.getProductId()).get();
                        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                        productDAO.update(product);
                    }
                    cartDAO.clear(buyerId);
                }

                conn.commit();
                return created;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Checkout failed", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Order> getOrdersByBuyer(long buyerId) {
        return orderDAO.findByBuyerId(buyerId);
    }

    public Order getOrderById(long orderId) {
        return orderDAO.findById(orderId)
                .orElseThrow(() -> AppException.notFound("Order not found"));
    }

    public Order updateStatus(long orderId, Order.Status status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderDAO.update(order);
    }
}