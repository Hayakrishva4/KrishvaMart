package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.ValidationUtil;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO;
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }
    public Product create(long sellerId, String name, String description, java.math.BigDecimal price,
      Integer stockQty, String category, String imageUrl) throws AppException {
        validateListing(name, price, stockQty, category);
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(name.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setCategory(category.trim());
        product.setImageUrl(imageUrl);
        return productDAO.insert(product);
    }

    public Product update(long productId, long sellerId, String name, String description,
      java.math.BigDecimal price, Integer stockQty, String category, String imageUrl)
            throws AppException {
        validateListing(name, price, stockQty, category);
        Product existing = getOwnedOrThrow(productId, sellerId);
        existing.setName(name.trim());
        existing.setDescription(description);
        existing.setPrice(price);
        existing.setStockQty(stockQty);
        existing.setCategory(category.trim());
        existing.setImageUrl(imageUrl);
        if (!productDAO.update(existing)) {
            throw new NotFoundException("Product not found");
        }
        return existing;
    }

    public void delete(long productId, long sellerId) throws AppException {
        getOwnedOrThrow(productId, sellerId);
        productDAO.delete(productId, sellerId);
    }

    public Product get(long productId) throws AppException {
        return productDAO.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public List<Product> search(String keyword, String category) throws AppException {
        return productDAO.search(keyword, category, true);
    }

    public com.krishva.krishvamart.dto.PagedResult<Product> search(com.krishva.krishvamart.dto.ProductSearchCriteria criteria)
            throws AppException {
        return productDAO.search(criteria);
    }

    public List<Product> listForSeller(long sellerId) throws AppException {
        return productDAO.findBySeller(sellerId);
    }

    public void moderateRemove(long productId, User admin) throws AppException {
        requireAdmin(admin);
        productDAO.setActive(productId, false);
    }

    private Product getOwnedOrThrow(long productId, long sellerId) throws AppException {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!product.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You do not own this listing");
        }
        return product;
    }

    private void requireAdmin(User user) throws ForbiddenException {
        if (user == null || user.getRole() != User.Role.ADMIN) {
            throw new ForbiddenException("Admin role required");
        }
    }

    private void validateListing(String name, java.math.BigDecimal price, Integer stockQty, String category)
            throws ValidationException {
        if (ValidationUtil.isBlank(name)) {
            throw new ValidationException("name", "Product name is required");
        }
        if (!ValidationUtil.isPositive(price)) {
            throw new ValidationException("price", "Price must be greater than zero");
        }
        if (stockQty == null || stockQty < 0) {
            throw new ValidationException("stockQty", "Stock quantity cannot be negative");
        }
        if (ValidationUtil.isBlank(category)) {
            throw new ValidationException("category", "Category is required");
        }
    }
}
