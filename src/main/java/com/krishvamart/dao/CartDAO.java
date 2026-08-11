package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.CartItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface CartDAO {
    List<CartItem> findByUser(long userId) throws DataAccessException;
    Optional<CartItem> findByUserAndProduct(long userId, long productId) throws DataAccessException;
    CartItem upsert(long userId, long productId, int quantity) throws DataAccessException;
    boolean updateQuantity(long userId, long productId, int quantity) throws DataAccessException;
    boolean remove(long userId, long productId) throws DataAccessException;
    void clear(long userId) throws DataAccessException;
    void clear(Connection conn, long userId) throws DataAccessException;
}
