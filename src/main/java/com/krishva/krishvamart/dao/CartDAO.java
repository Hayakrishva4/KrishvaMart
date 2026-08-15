package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.CartItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Data access for the {@code cart_items} table. All SQL lives in the implementation, PreparedStatement only. */
public interface CartDAO {

    /** F4: all line items in a user's cart, with product name/price joined in, oldest first. */
    List<CartItem> findByUser(long userId) throws DataAccessException;

    /** Looks up a single cart line by (user, product), if the product is currently in the cart. */
    Optional<CartItem> findByUserAndProduct(long userId, long productId) throws DataAccessException;

    /** Inserts a new cart line or overwrites the quantity of an existing one for (user, product); returns the result row. */
    CartItem upsert(long userId, long productId, int quantity) throws DataAccessException;

    /** F4: updates the quantity of an existing cart line. Returns false if the item wasn't in the cart. */
    boolean updateQuantity(long userId, long productId, int quantity) throws DataAccessException;

    /** F4: removes one product from a user's cart. Returns false if it wasn't there. */
    boolean remove(long userId, long productId) throws DataAccessException;

    /** Empties a user's entire cart (used after a successful checkout). */
    void clear(long userId) throws DataAccessException;

    /** Same as {@link #clear(long)} but participates in a caller-managed transaction (checkout). */
    void clear(Connection conn, long userId) throws DataAccessException;
}
