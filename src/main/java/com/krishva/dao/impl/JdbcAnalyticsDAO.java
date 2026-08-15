package com.krishva.krishvamart.dao.impl;

import com.krishva.krishvamart.dao.AnalyticsDAO;
import com.krishva.krishvamart.dto.ProductSalesDTO;
import com.krishva.krishvamart.dto.SellerSalesSummaryDTO;
import com.krishva.krishvamart.exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * All SQL for the O3 seller sales dashboard lives here, PreparedStatement
 * only (Section 2, Rule 1). Revenue and unit counts are computed only from
 * DELIVERED orders - a shipped-but-not-yet-delivered order isn't a
 * completed sale yet.
 */
public class JdbcAnalyticsDAO implements AnalyticsDAO {

    private final DataSource dataSource;

    public JdbcAnalyticsDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public SellerSalesSummaryDTO sellerSummary(long sellerId) throws DataAccessException {
        String totalsSql = "SELECT COUNT(DISTINCT o.id) AS order_count, "
                + "COALESCE(SUM(oi.quantity), 0) AS units_sold, "
                + "COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue "
                + "FROM orders o "
                + "JOIN order_items oi ON oi.order_id = o.id "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE p.seller_id = ? AND o.status = 'DELIVERED'";

        String byProductSql = "SELECT p.id AS product_id, p.name AS product_name, "
                + "COALESCE(SUM(oi.quantity), 0) AS units_sold, "
                + "COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue "
                + "FROM products p "
                + "LEFT JOIN order_items oi ON oi.product_id = p.id "
                + "LEFT JOIN orders o ON o.id = oi.order_id AND o.status = 'DELIVERED' "
                + "WHERE p.seller_id = ? "
                + "GROUP BY p.id, p.name "
                + "ORDER BY revenue DESC";

        try (Connection conn = dataSource.getConnection()) {
            long orderCount;
            long unitsSold;
            BigDecimal revenue;
            try (PreparedStatement ps = conn.prepareStatement(totalsSql)) {
                ps.setLong(1, sellerId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderCount = rs.getLong("order_count");
                    unitsSold = rs.getLong("units_sold");
                    revenue = rs.getBigDecimal("revenue");
                }
            }

            List<ProductSalesDTO> byProduct = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(byProductSql)) {
                ps.setLong(1, sellerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        byProduct.add(new ProductSalesDTO(
                                rs.getLong("product_id"),
                                rs.getString("product_name"),
                                rs.getLong("units_sold"),
                                rs.getBigDecimal("revenue")));
                    }
                }
            }

            return SellerSalesSummaryDTO.builder()
                    .totalOrders(orderCount)
                    .totalUnitsSold(unitsSold)
                    .totalRevenue(revenue)
                    .byProduct(byProduct)
                    .build();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute seller sales summary for seller " + sellerId, e);
        }
    }
}
