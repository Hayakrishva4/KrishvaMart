package com.krishva.krishvamart.service;

import java.math.BigDecimal;
import java.util.List;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.CartItem;
import com.krishva.krishvamart.model.Product;

public class CartService {
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartService(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    public CartItem addItem(long userId, long productId, int quantity) throws AppException {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be at least 1");
        }
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!product.isActive()) {
            throw new ConflictException("This product is no longer available");
        }
       int existingQty = 0;
    java.util.Optional<CartItem> existingItem = cartDAO.findByUserAndProduct(userId, productId);
     if (existingItem != null && existingItem.isPresent()) {
      CartItem item = existingItem.get();
     if (item != null) {
        existingQty = item.getQuantity();
      }
    }
        int newQty = existingQty + quantity;
        if (newQty > product.getStockQty()) {
            throw new ConflictException("Only " + product.getStockQty() + " units in stock");
        }
        return cartDAO.upsert(userId, productId, newQty);
    }

    public void updateQuantity(long userId, long productId, int quantity) throws AppException {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "Quantity must be at least 1");
        }
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (quantity > product.getStockQty()) {
            throw new ConflictException("Only " + product.getStockQty() + " units in stock");
        }
        if (!cartDAO.updateQuantity(userId, productId, quantity)) {
            throw new NotFoundException("Item not in cart");
        }
    }

    public void removeItem(long userId, long productId) throws AppException {
        if (!cartDAO.remove(userId, productId)) {
            throw new NotFoundException("Item not in cart");
        }
    }

    public List<CartItem> view(long userId) throws AppException {
        return cartDAO.findByUser(userId);
    }

    public BigDecimal runningTotal(long userId) throws AppException {
        List<CartItem> items = view(userId);
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (CartItem item : items) {
                if (item != null && item.getLineTotal() != null) {
                    total = total.add(item.getLineTotal());
                }
            }
        }
        return total;
    }
}