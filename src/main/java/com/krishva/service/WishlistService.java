package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.WishlistDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.model.WishlistItem;

import java.util.List;

/** Business rules for O1 (wishlist / save-for-later). No JDBC here - all persistence goes through WishlistDAO. */
public class WishlistService {

    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;

    public WishlistService(WishlistDAO wishlistDAO, ProductDAO productDAO) {
        this.wishlistDAO = wishlistDAO;
        this.productDAO = productDAO;
    }

    /** O1: saves a product to the buyer's wishlist; throws NotFoundException if the product doesn't exist. */
    public WishlistItem add(long userId, long productId) throws AppException {
        productDAO.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        return wishlistDAO.add(userId, productId);
    }

    /** O1: removes a product from the buyer's wishlist. Throws NotFoundException if it wasn't saved. */
    public void remove(long userId, long productId) throws AppException {
        if (!wishlistDAO.remove(userId, productId)) {
            throw new NotFoundException("Item not in wishlist");
        }
    }

    /** O1: the buyer's full wishlist, newest first. */
    public List<WishlistItem> view(long userId) throws AppException {
        return wishlistDAO.findByUser(userId);
    }
}
