package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.WishlistDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.model.WishlistItem;
import java.util.List;

public class WishlistService {
    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;
    public WishlistService(WishlistDAO wishlistDAO, ProductDAO productDAO) {
        this.wishlistDAO = wishlistDAO;
        this.productDAO = productDAO;
    }
    public WishlistItem add(long userId, long productId) throws AppException {
        productDAO.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        return wishlistDAO.add(userId, productId);
    }

    public void remove(long userId, long productId) throws AppException {
        if (!wishlistDAO.remove(userId, productId)) {
            throw new NotFoundException("Item not in wishlist");
        }
    }

    public List<WishlistItem> view(long userId) throws AppException {
        return wishlistDAO.findByUser(userId);
    }
}
