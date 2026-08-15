package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Data access for {@code orders}/{@code order_items}. All SQL lives in the implementation, PreparedStatement only. */
public interface OrderDAO {

    /** F5: inserts the order header row within the caller's checkout transaction; returns it with the generated id. */
    Order insert(Connection conn, Order order) throws DataAccessException;

    /** F5: inserts one order line item within the caller's checkout transaction. */
    void insertItem(Connection conn, long orderId, long productId, int quantity, BigDecimal unitPrice)
            throws DataAccessException;

    /** Looks up an order by id, with its line items eagerly loaded. */
    Optional<Order> findById(long id) throws DataAccessException;

    /** F6: a buyer's full order history, newest first, with line items eagerly loaded. */
    List<Order> findByBuyer(long buyerId) throws DataAccessException;

    /** F6: every order containing at least one of this seller's products, newest first. */
    List<Order> findBySeller(long sellerId) throws DataAccessException;

    /** F7: every order in the system, for the admin view. */
    List<Order> findAll() throws DataAccessException;

    /** O2: applies a status transition (validated by the service layer before this is called). */
    boolean updateStatus(long orderId, Order.Status status) throws DataAccessException;

    /** F8: true if the buyer has a DELIVERED order containing this product (eligibility check for reviews). */
    boolean hasBuyerCompletedOrderForProduct(long buyerId, long productId) throws DataAccessException;
}
