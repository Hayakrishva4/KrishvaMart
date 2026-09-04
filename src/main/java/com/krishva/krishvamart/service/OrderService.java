package com.krishva.krishvamart.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.CartItem;
import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.OrderItem;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.ValidationUtil;

public class OrderService {

    private final DataSource dataSource;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;

    public OrderService(
            DataSource dataSource,
            OrderDAO orderDAO,
            ProductDAO productDAO,
            CartDAO cartDAO) {
        this.dataSource = dataSource;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.cartDAO = cartDAO;
    }

    public Order checkout(
            long buyerId,
            boolean mockPaymentConfirmed,
            String shippingAddress) throws AppException {

        if (!mockPaymentConfirmed) {
            throw new ValidationException(
                    "payment",
                    "Mock payment confirmation is required to place an order");
        }

        if (ValidationUtil.isBlank(shippingAddress)) {
            throw new ValidationException(
                    "shippingAddress",
                    "A shipping address is required");
        }

        List<CartItem> cartItems = cartDAO.findByUser(buyerId);

        if (cartItems.isEmpty()) {
            throw new ValidationException(
                    "cart",
                    "Your cart is empty");
        }

        BigDecimal total = cartItems.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setStatus(Order.Status.CONFIRMED);
        order.setTotalAmount(total);
        order.setShippingAddress(shippingAddress.trim());

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Order saved = orderDAO.insert(conn, order);

                for (CartItem item : cartItems) {
                    boolean stockOk = productDAO.adjustStock(
                            conn,
                            item.getProductId(),
                            -item.getQuantity());

                    if (!stockOk) {
                        throw new ConflictException(
                                "\"" + item.getProductName()
                                        + "\" no longer has enough stock");
                    }

                    orderDAO.insertItem(
                            conn,
                            saved.getId(),
                            item.getProductId(),
                            item.getQuantity(),
                            item.getUnitPrice());
                }

                cartDAO.clear(conn, buyerId);
                conn.commit();

                return orderDAO.findById(saved.getId()).orElse(saved);

            } catch (AppException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException(
                        "Checkout transaction failed",
                        e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to open checkout transaction",
                    e);
        }
    }

    public void cancelOrder(
            long orderId,
            User requester) throws AppException {

        Order order = get(orderId, requester);

        boolean isOwner = order.getBuyerId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException(
                    "You can only cancel your own orders");
        }

        if (order.getStatus() != Order.Status.PENDING
                && order.getStatus() != Order.Status.CONFIRMED) {
            throw new ValidationException(
                    "status",
                    "Only PENDING or CONFIRMED orders can be cancelled");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        boolean success = productDAO.adjustStock(
                                conn,
                                item.getProductId(),
                                item.getQuantity());

                        if (!success) {
                            throw new DataAccessException(
                                    "Failed to restore stock for product ID: "
                                            + item.getProductId(),
                                    new RuntimeException(
                                            "Stock adjustment failed"));
                        }
                    }
                }

                boolean updated = orderDAO.updateStatus(
                        orderId,
                        Order.Status.CANCELLED);

                if (!updated) {
                    throw new DataAccessException(
                            "Failed to update order status to cancelled",
                            new RuntimeException(
                                    "Update status returned false"));
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();

                if (e instanceof AppException) {
                    throw (AppException) e;
                }

                throw new DataAccessException(
                        "Transaction failed during order cancellation",
                        e);
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Database connection error during cancellation",
                    e);
        }
    }

    public List<Order> historyForBuyer(long buyerId)
            throws AppException {
        return orderDAO.findByBuyer(buyerId);
    }

    public List<Order> incomingForSeller(long sellerId)
            throws AppException {
        return orderDAO.findBySeller(sellerId);
    }

    public List<Order> listAllForAdmin(User admin)
            throws AppException {
        requireAdmin(admin);
        return orderDAO.findAll();
    }

    public Order get(long orderId, User requester)
            throws AppException {

        Order order = orderDAO.findById(orderId)
                .orElseThrow(
                        () -> new NotFoundException("Order not found"));

        boolean isOwner = order.getBuyerId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        boolean isSellerOnOrder = false;

        if (requester.getRole() == User.Role.SELLER) {
            for (var item : order.getItems()) {
                var product = productDAO.findById(
                        item.getProductId());

                if (product.isPresent()
                        && product.get().getSellerId()
                                .equals(requester.getId())) {
                    isSellerOnOrder = true;
                    break;
                }
            }
        }

        if (!isOwner && !isAdmin && !isSellerOnOrder) {
            throw new ForbiddenException(
                    "You cannot view this order");
        }

        return order;
    }

    public void advanceStatus(
            long orderId,
            Order.Status newStatus,
            User actor) throws AppException {

        if (actor.getRole() != User.Role.SELLER
                && actor.getRole() != User.Role.ADMIN) {
            throw new ForbiddenException(
                    "Only a seller or admin can update order status");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(
                        () -> new NotFoundException("Order not found"));

        if (!isValidTransition(
                order.getStatus(),
                newStatus)) {
            throw new ValidationException(
                    "status",
                    "Cannot move order from "
                            + order.getStatus()
                            + " to "
                            + newStatus);
        }

        orderDAO.updateStatus(orderId, newStatus);
    }

    private boolean isValidTransition(
            Order.Status from,
            Order.Status to) {

        if (to == Order.Status.CANCELLED) {
            return from == Order.Status.PENDING
                    || from == Order.Status.CONFIRMED;
        }

        return switch (from) {
            case PENDING -> to == Order.Status.CONFIRMED;
            case CONFIRMED -> to == Order.Status.SHIPPED;
            case SHIPPED -> to == Order.Status.DELIVERED;
            default -> false;
        };
    }

    private void requireAdmin(User user)
            throws ForbiddenException {

        if (user == null
                || user.getRole() != User.Role.ADMIN) {
            throw new ForbiddenException(
                    "Admin role required");
        }
    }
}