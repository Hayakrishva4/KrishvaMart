package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.ReviewRequestDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Review;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * F8: product reviews and star ratings on completed orders. GET is public
 * (see AuthFilter), POST requires a logged-in buyer.
 * Mapped at /api/v1/reviews and /api/v1/reviews/product/{productId}.
 */
@WebServlet(urlPatterns = {"/api/v1/reviews", "/api/v1/reviews/*"})
public class ReviewServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || !pathInfo.startsWith("/product/")) {
                throw new ValidationException("productId", "Use /api/v1/reviews/product/{productId}");
            }
            long productId = Long.parseLong(pathInfo.substring("/product/".length()));
            List<Review> reviews = services().reviewService().forProduct(productId);
            double average = services().reviewService().averageRating(productId);
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, Map.of("reviews", reviews, "averageRating", average));
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            if (user.getRole() != User.Role.BUYER) {
                throw new ForbiddenException("Only buyers can submit reviews");
            }
            ReviewRequestDTO body = readBody(req, ReviewRequestDTO.class);
            if (body == null || body.getProductId() == null || body.getOrderId() == null || body.getRating() == null) {
                throw new ValidationException("rating", "productId, orderId and rating are required");
            }
            Review review = services().reviewService().submit(user.getId(), body.getOrderId(), body.getProductId(),
                    body.getRating(), body.getComment());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_CREATED, review);
        } catch (AppException e) {
            handleError(resp, e);
        }
    }
}
