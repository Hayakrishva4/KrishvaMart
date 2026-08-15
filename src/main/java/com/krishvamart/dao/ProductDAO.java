package com.krishva.krishvamart.dao;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.model.Product;
import java.util.List;
import java.util.Optional;
public interface ProductDAO {
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findBySellerId(Long sellerId);
    List<Product> search(String keyword, String category, boolean inStockOnly);
    List<Product> search(ProductSearchCriteria criteria);
    Product create(Product product);
    Product update(Product product);
    boolean delete(Long id);
}