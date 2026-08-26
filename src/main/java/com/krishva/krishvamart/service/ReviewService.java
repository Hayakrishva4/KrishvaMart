package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ReviewDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.Review;
import com.krishva.krishvamart.util.ValidationUtil;

import java.util.List;

/** Business rules for product reviews and star ratings on completed orders (F8). No JDBC here. */
public class ReviewService {

    private final ReviewDAO reviewDAO;
    private final OrderDAO orderDAO;

    public ReviewService(ReviewDAO reviewDAO, OrderDAO orderDAO) {
        this.reviewDAO = reviewDAO;
        this.orderDAO = orderDAO;
    }

    /** F8: submits a star rating + comment; only allowed once per (buyer, order, product) on a DELIVERED order. */
    public Review submit(long userId, long orderId, long productId, int rating, String comment) throws AppException {
        if (!ValidationUtil.isValidRating(rating)) {
            throw new ValidationException("rating", "Rating must be between 1 and 5");
        }
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ValidationException("orderId", "Order not found"));
        if (!order.getBuyerId().equals(userId)) {
            throw new ForbiddenException("You can only review your own orders");
        }
        if (order.getStatus() != Order.Status.DELIVERED) {
            throw new ConflictException("You can only review products from delivered orders");
        }
        boolean productInOrder = order.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
        if (!productInOrder) {
            throw new ValidationException("productId", "This product was not part of that order");
        }
        if (reviewDAO.existsByUserAndOrderAndProduct(userId, orderId, productId)) {
            throw new ConflictException("You already reviewed this product for this order");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);
        return reviewDAO.insert(review);
    }

    /** F8: all reviews for a product, newest first. */
    public List<Review> forProduct(long productId) throws AppException {
        return reviewDAO.findByProduct(productId);
    }

    /** F8: mean star rating for a product; 0.0 if it has no reviews yet. */
    public double averageRating(long productId) throws AppException {
        return reviewDAO.averageRating(productId);
    }
}
