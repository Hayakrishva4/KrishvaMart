package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.Review;

import java.util.List;

/** Data access for the {@code reviews} table. All SQL lives in the implementation, PreparedStatement only. */
public interface ReviewDAO {

    /** F8: persists a star rating + comment and returns it with the generated id populated. */
    Review insert(Review review) throws DataAccessException;

    /** F8: all reviews for a product, newest first, with the reviewer's display name joined in. */
    List<Review> findByProduct(long productId) throws DataAccessException;

    /** Enforces "one review per (user, order, product)" - checked before insert in the service layer. */
    boolean existsByUserAndOrderAndProduct(long userId, long orderId, long productId) throws DataAccessException;

    /** Mean star rating for a product; 0.0 if it has no reviews yet. */
    double averageRating(long productId) throws DataAccessException;
}
