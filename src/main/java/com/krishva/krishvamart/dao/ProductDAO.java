package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.dto.PagedResult;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Product;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/** Data access for the {@code products} table. All SQL lives in the implementation, PreparedStatement only. */
public interface ProductDAO {

    /** Persists a new listing (F2) and returns it with the generated id populated; active defaults to true. */
    Product insert(Product product) throws DataAccessException;

    /** Looks up a product by primary key, regardless of active status. */
    Optional<Product> findById(long id) throws DataAccessException;

    /** F3: buyer browse/search. Filters are ANDed together; a null/blank filter is skipped. */
    List<Product> search(String keyword, String category, boolean activeOnly) throws DataAccessException;

    /** F3 extended: price range, sort order, and pagination on top of keyword/category (real-marketplace-style browse). */
    PagedResult<Product> search(ProductSearchCriteria criteria) throws DataAccessException;

    /** Every listing (active or not) owned by the given seller, for the seller dashboard. */
    List<Product> findBySeller(long sellerId) throws DataAccessException;

    /** F2 edit: updates listing fields. Returns false if no row matched (wrong id or not owned by sellerId). */
    boolean update(Product product) throws DataAccessException;

    /** Adds {@code delta} (negative to decrement) to stock_qty; fails atomically if the result would go negative. */
    boolean adjustStock(long productId, int delta) throws DataAccessException;

    /** Same as {@link #adjustStock(long, int)} but participates in a caller-managed transaction. */
    boolean adjustStock(Connection conn, long productId, int delta) throws DataAccessException;

    /** F7: admin moderation - deactivates (active=false) or reactivates a listing without deleting it. */
    boolean setActive(long productId, boolean active) throws DataAccessException;

    /** F2 delete: removes a listing. Returns false if no row matched (wrong id or not owned by sellerId). */
    boolean delete(long productId, long sellerId) throws DataAccessException;
}
