package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    Order insert(Connection conn, Order order) throws DataAccessException;
    void insertItem(Connection conn, long orderId, long productId, int quantity, BigDecimal unitPrice)
            throws DataAccessException;
    Optional<Order> findById(long id) throws DataAccessException;
    List<Order> findByBuyer(long buyerId) throws DataAccessException;
    List<Order> findBySeller(long sellerId) throws DataAccessException;
    List<Order> findAll() throws DataAccessException;
    boolean updateStatus(long orderId, Order.Status status) throws DataAccessException;
    boolean hasBuyerCompletedOrderForProduct(long buyerId, long productId) throws DataAccessException;
}
