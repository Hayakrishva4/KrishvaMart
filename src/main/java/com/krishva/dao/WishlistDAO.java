package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.WishlistItem;

import java.util.List;

/** Data access for the {@code wishlist_items} table (O1). All SQL lives in the implementation, PreparedStatement only. */
public interface WishlistDAO {

    /** O1: saves a product to a buyer's wishlist; a no-op (via MERGE) if already saved. */
    WishlistItem add(long userId, long productId) throws DataAccessException;

    /** O1: removes a product from a buyer's wishlist. Returns false if it wasn't there. */
    boolean remove(long userId, long productId) throws DataAccessException;

    /** O1: a buyer's full wishlist, with current product name/price/stock joined in, newest first. */
    List<WishlistItem> findByUser(long userId) throws DataAccessException;

    /** True if the given product is already on the user's wishlist (used to toggle the UI heart icon). */
    boolean exists(long userId, long productId) throws DataAccessException;
}
