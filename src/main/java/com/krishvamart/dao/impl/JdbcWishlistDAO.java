package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.WishlistDAO;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.WishlistItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** All SQL for the wishlist_items table lives here, PreparedStatement only (Section 2, Rule 1). */
public class JdbcWishlistDAO implements WishlistDAO {

    private final DataSource dataSource;

    public JdbcWishlistDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public WishlistItem add(long userId, long productId) throws DataAccessException {
        String sql = "MERGE INTO wishlist_items (user_id, product_id) KEY (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add wishlist item", e);
        }
        return findOne(userId, productId);
    }

    @Override
    public boolean remove(long userId, long productId) throws DataAccessException {
        String sql = "DELETE FROM wishlist_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove wishlist item", e);
        }
    }

    @Override
    public List<WishlistItem> findByUser(long userId) throws DataAccessException {
        String sql = "SELECT w.id, w.user_id, w.product_id, w.created_at, "
                + "p.name AS product_name, p.price AS product_price, p.image_url AS product_image_url, "
                + "p.stock_qty AS product_stock_qty "
                + "FROM wishlist_items w JOIN products p ON p.id = w.product_id "
                + "WHERE w.user_id = ? ORDER BY w.created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<WishlistItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(map(rs));
                }
                return items;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load wishlist for user " + userId, e);
        }
    }

    @Override
    public boolean exists(long userId, long productId) throws DataAccessException {
        String sql = "SELECT 1 FROM wishlist_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check wishlist membership", e);
        }
    }

    private WishlistItem findOne(long userId, long productId) throws DataAccessException {
        String sql = "SELECT w.id, w.user_id, w.product_id, w.created_at, "
                + "p.name AS product_name, p.price AS product_price, p.image_url AS product_image_url, "
                + "p.stock_qty AS product_stock_qty "
                + "FROM wishlist_items w JOIN products p ON p.id = w.product_id "
                + "WHERE w.user_id = ? AND w.product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Wishlist item vanished after upsert", null);
                }
                return map(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load wishlist item", e);
        }
    }

    private WishlistItem map(ResultSet rs) throws SQLException {
        WishlistItem item = new WishlistItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setProductPrice(rs.getBigDecimal("product_price"));
        item.setProductImageUrl(rs.getString("product_image_url"));
        item.setProductStockQty(rs.getInt("product_stock_qty"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        return item;
    }
}
