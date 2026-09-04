package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.CartItem;
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

public class JdbcCartDAO implements CartDAO {
    private final DataSource dataSource;

    public JdbcCartDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CartItem> findByUser(long userId) throws DataAccessException {
        String sql = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, ci.created_at, "
                + "p.name AS product_name, p.price AS unit_price "
                + "FROM cart_items ci JOIN products p ON p.id = ci.product_id "
                + "WHERE ci.user_id = ? ORDER BY ci.created_at";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load cart for user " + userId, e);
        }
    }

    @Override
    public Optional<CartItem> findByUserAndProduct(long userId, long productId) throws DataAccessException {
        String sql = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, ci.created_at, "
                + "p.name AS product_name, p.price AS unit_price "
                + "FROM cart_items ci JOIN products p ON p.id = ci.product_id "
                + "WHERE ci.user_id = ? AND ci.product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load cart item", e);
        }
    }

    @Override
    public CartItem upsert(long userId, long productId, int quantity) throws DataAccessException {
        String sql = "MERGE INTO cart_items (user_id, product_id, quantity) KEY (user_id, product_id) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add item to cart", e);
        }
        return findByUserAndProduct(userId, productId)
                .orElseThrow(() -> new DataAccessException("Cart item vanished after upsert", null));
    }

    @Override
    public boolean updateQuantity(long userId, long productId, int quantity) throws DataAccessException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, userId);
            ps.setLong(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update cart item quantity", e);
        }
    }

    @Override
    public boolean remove(long userId, long productId) throws DataAccessException {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove cart item", e);
        }
    }

    @Override
    public void clear(long userId) throws DataAccessException {
        try (Connection conn = dataSource.getConnection()) {
            clear(conn, userId);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear cart for user " + userId, e);
        }
    }

    @Override
    public void clear(Connection conn, long userId) throws DataAccessException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear cart for user " + userId, e);
        }
    }

    private CartItem map(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setQuantity(rs.getInt("quantity"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        return item;
    }
}
