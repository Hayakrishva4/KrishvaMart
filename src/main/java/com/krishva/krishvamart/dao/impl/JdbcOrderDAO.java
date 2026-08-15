package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.OrderItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** All SQL for orders/order_items lives here, PreparedStatement only (Section 2, Rule 1). */
public class JdbcOrderDAO implements OrderDAO {

    private final DataSource dataSource;

    public JdbcOrderDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Order insert(Connection conn, Order order) throws DataAccessException {
        String sql = "INSERT INTO orders (buyer_id, status, total_amount, shipping_address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getBuyerId());
            ps.setString(2, order.getStatus().name());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getShippingAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getLong(1));
                }
            }
            return order;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert order", e);
        }
    }

    @Override
    public void insertItem(Connection conn, long orderId, long productId, int quantity, BigDecimal unitPrice)
            throws DataAccessException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert order item", e);
        }
    }

    @Override
    public Optional<Order> findById(long id) throws DataAccessException {
        String sql = "SELECT id, buyer_id, status, total_amount, shipping_address, created_at FROM orders WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Order order = map(rs);
                order.setItems(loadItems(conn, order.getId()));
                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load order " + id, e);
        }
    }

    @Override
    public List<Order> findByBuyer(long buyerId) throws DataAccessException {
        String sql = "SELECT id, buyer_id, status, total_amount, shipping_address, created_at FROM orders "
                + "WHERE buyer_id = ? ORDER BY created_at DESC";
        return queryList(sql, buyerId);
    }

    @Override
    public List<Order> findBySeller(long sellerId) throws DataAccessException {
        String sql = "SELECT DISTINCT o.id, o.buyer_id, o.status, o.total_amount, o.created_at "
                + "FROM orders o "
                + "JOIN order_items oi ON oi.order_id = o.id "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE p.seller_id = ? ORDER BY o.created_at DESC";
        return queryList(sql, sellerId);
    }

    @Override
    public List<Order> findAll() throws DataAccessException {
        String sql = "SELECT id, buyer_id, status, total_amount, shipping_address, created_at FROM orders ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order order = map(rs);
                order.setItems(loadItems(conn, order.getId()));
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list all orders", e);
        }
    }

    @Override
    public boolean updateStatus(long orderId, Order.Status status) throws DataAccessException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update order status", e);
        }
    }

    @Override
    public boolean hasBuyerCompletedOrderForProduct(long buyerId, long productId) throws DataAccessException {
        String sql = "SELECT 1 FROM orders o JOIN order_items oi ON oi.order_id = o.id "
                + "WHERE o.buyer_id = ? AND oi.product_id = ? AND o.status = 'DELIVERED' LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, buyerId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check order completion", e);
        }
    }

    private List<Order> queryList(String sql, long param) throws DataAccessException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    Order order = map(rs);
                    order.setItems(loadItems(conn, order.getId()));
                    orders.add(order);
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query orders", e);
        }
    }

    private List<OrderItem> loadItems(Connection conn, long orderId) throws SQLException {
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, oi.created_at, "
                + "p.name AS product_name FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + "WHERE oi.order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        item.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    items.add(item);
                }
                return items;
            }
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setStatus(Order.Status.valueOf(rs.getString("status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setShippingAddress(rs.getString("shipping_address"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        return order;
    }
}
