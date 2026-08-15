package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Product;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Product insert(Product product) throws DataAccessException;
    Optional<Product> findById(long id) throws DataAccessException;
    List<Product> search(String keyword, String category, boolean activeOnly) throws DataAccessException;
    List<Product> findBySeller(long sellerId) throws DataAccessException;
    boolean update(Product product) throws DataAccessException;
    boolean adjustStock(long productId, int delta) throws DataAccessException;
    boolean adjustStock(Connection conn, long productId, int delta) throws DataAccessException;
    boolean setActive(long productId, boolean active) throws DataAccessException;
    boolean delete(long productId, long sellerId) throws DataAccessException;
}
