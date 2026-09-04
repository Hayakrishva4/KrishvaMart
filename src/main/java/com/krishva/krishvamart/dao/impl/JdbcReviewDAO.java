package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.ReviewDAO;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Review;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcReviewDAO implements ReviewDAO {
    private final DataSource dataSource;

    public JdbcReviewDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Review insert(Review review) throws DataAccessException {
        String sql = "INSERT INTO reviews (product_id, user_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setLong(3, review.getOrderId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getLong(1));
                }
            }
            return review;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert review", e);
        }
    }

    @Override
    public List<Review> findByProduct(long productId) throws DataAccessException {
        String sql = "SELECT r.id, r.product_id, r.user_id, r.order_id, r.rating, r.comment, r.created_at, "
                + "u.name AS user_name FROM reviews r JOIN users u ON u.id = r.user_id "
                + "WHERE r.product_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (rs.next()) {
                    reviews.add(map(rs));
                }
                return reviews;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load reviews for product " + productId, e);
        }
    }

    @Override
    public boolean existsByUserAndOrderAndProduct(long userId, long orderId, long productId) throws DataAccessException {
        String sql = "SELECT 1 FROM reviews WHERE user_id = ? AND order_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, orderId);
            ps.setLong(3, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check existing review", e);
        }
    }

    @Override
    public double averageRating(long productId) throws DataAccessException {
        String sql = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
                return 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute average rating for product " + productId, e);
        }
    }

    private Review map(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setProductId(rs.getLong("product_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setUserName(rs.getString("user_name"));
        review.setOrderId(rs.getLong("order_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toLocalDateTime());
        }
        return review;
    }
}
