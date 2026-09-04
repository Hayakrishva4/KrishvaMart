package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dto.PagedResult;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Product;
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

public class JdbcProductDAO implements ProductDAO {

    private final DataSource dataSource;

    public JdbcProductDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Product insert(Product product) throws DataAccessException {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty());
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }
            product.setActive(true);
            return product;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert product", e);
        }
    }

    @Override
    public Optional<Product> findById(long id) throws DataAccessException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load product " + id, e);
        }
    }

    @Override
    public List<Product> search(String keyword, String category, boolean activeOnly) throws DataAccessException {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (activeOnly) {
            sql.append("AND active = TRUE ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String pattern = "%" + keyword.toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (category != null && !category.isBlank()) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        sql.append("ORDER BY created_at DESC, id DESC");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(map(rs));
                }
                return products;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search products", e);
        }
    }

    @Override
    public PagedResult<Product> search(ProductSearchCriteria criteria) throws DataAccessException {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (criteria.isActiveOnly()) {
            where.append("AND active = TRUE ");
        }
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            where.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            where.append("AND category = ? ");
            params.add(criteria.getCategory());
        }
        if (criteria.getMinPrice() != null) {
            where.append("AND price >= ? ");
            params.add(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            where.append("AND price <= ? ");
            params.add(criteria.getMaxPrice());
        }

        String orderBy;
        if (criteria.getSortBy() == null) {
            orderBy = " ORDER BY id DESC ";
        } else {
            orderBy = switch (criteria.getSortBy()) {
                case PRICE_ASC -> " ORDER BY price ASC, id ASC ";
                case PRICE_DESC -> " ORDER BY price DESC, id DESC ";
                case NEWEST -> " ORDER BY created_at DESC, id DESC ";
                case RELEVANCE -> " ORDER BY created_at DESC, id DESC ";
            };
        }

        int page = criteria.getPage() <= 0 ? 1 : criteria.getPage();
        int pageSize = criteria.getPageSize() <= 0 ? 12 : criteria.getPageSize();
        int offset = (page - 1) * pageSize;

        try (Connection conn = dataSource.getConnection()) {
            long totalItems;
            try (PreparedStatement countPs = conn.prepareStatement("SELECT COUNT(*) FROM products " + where)) {
                bindParams(countPs, params);
                try (ResultSet rs = countPs.executeQuery()) {
                    rs.next();
                    totalItems = rs.getLong(1);
                }
            }

            String pageSql = "SELECT * FROM products " + where + orderBy + " LIMIT ? OFFSET ?";
            List<Product> items = new ArrayList<>();
            try (PreparedStatement pagePs = conn.prepareStatement(pageSql)) {
                int idx = bindParams(pagePs, params);
                pagePs.setInt(idx, pageSize);
                pagePs.setInt(idx + 1, offset);
                try (ResultSet rs = pagePs.executeQuery()) {
                    while (rs.next()) {
                        items.add(map(rs));
                    }
                }
            }
            return new PagedResult<>(items, page, pageSize, totalItems);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search products with criteria", e);
        }
    }

    private int bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        int i = 1;
        for (Object param : params) {
            ps.setObject(i++, param);
        }
        return i;
    }

    @Override
    public List<Product> findBySeller(long sellerId) throws DataAccessException {
        String sql = "SELECT * FROM products WHERE seller_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(map(rs));
                }
                return products;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load seller products", e);
        }
    }

    @Override
    public boolean update(Product product) throws DataAccessException {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, "
                + "category = ?, image_url = ? WHERE id = ? AND seller_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQty());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setLong(7, product.getId());
            ps.setLong(8, product.getSellerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update product " + product.getId(), e);
        }
    }

    @Override
    public boolean adjustStock(long productId, int delta) throws DataAccessException {
        try (Connection conn = dataSource.getConnection()) {
            return adjustStock(conn, productId, delta);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to adjust stock for product " + productId, e);
        }
    }

    @Override
    public boolean adjustStock(Connection conn, long productId, int delta) throws DataAccessException {
        String sql = "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ? AND stock_qty + ? >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setLong(2, productId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to adjust stock for product " + productId, e);
        }
    }

    @Override
    public boolean setActive(long productId, boolean active) throws DataAccessException {
        String sql = "UPDATE products SET active = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to set active flag for product " + productId, e);
        }
    }

    @Override
    public boolean delete(long productId, long sellerId) throws DataAccessException {
        String sql = "DELETE FROM products WHERE id = ? AND seller_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, sellerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete product " + productId, e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setSellerId(rs.getLong("seller_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStockQty(rs.getInt("stock_qty"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        product.setActive(rs.getBoolean("active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        return product;
    }
}