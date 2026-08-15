package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.model.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductDAO implements ProductDAO {

    private final DataSource dataSource;

    public JdbcProductDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE seller_id = ? ORDER BY id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Product> search(String keyword, String category, boolean inStockOnly) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }

        if (inStockOnly) {
            sql.append(" AND stock_quantity > 0");
        }

        sql.append(" ORDER BY id DESC");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Product> search(ProductSearchCriteria criteria) {
        if (criteria == null) {
            return findAll();
        }
        return search(criteria.getKeyword(), criteria.getCategory(), criteria.isInStockOnly());
    }

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products (seller_id, title, description, price, stock_quantity, category, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getTitle());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                }
            }
            return product;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Product update(Product product) {
        String sql = "UPDATE products SET title = ?, description = ?, price = ?, stock_quantity = ?, " +
                     "category = ?, image_url = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getTitle());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            ps.executeUpdate();
            return product;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
