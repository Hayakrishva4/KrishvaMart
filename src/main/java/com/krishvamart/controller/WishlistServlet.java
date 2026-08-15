package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.CartItemRequestDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.model.WishlistItem;
import com.krishva.krishvamart.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * O1: wishlist / save-for-later. Mapped at /api/v1/wishlist (view/add) and
 * /api/v1/wishlist/{productId} (remove). Buyer-only, like the cart.
 */
@WebServlet(urlPatterns = {"/api/v1/wishlist", "/api/v1/wishlist/*"})
public class WishlistServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireBuyer(req);
            List<WishlistItem> items = services().wishlistService().view(user.getId());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, items);
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireBuyer(req);
            CartItemRequestDTO body = readBody(req, CartItemRequestDTO.class);
            if (body == null || body.getProductId() == null) {
                throw new ValidationException("productId", "productId is required");
            }
            WishlistItem item = services().wishlistService().add(user.getId(), body.getProductId());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_CREATED, item);
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireBuyer(req);
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                throw new NotFoundException("Product id required in path");
            }
            long productId = Long.parseLong(pathInfo.substring(1));
            services().wishlistService().remove(user.getId(), productId);
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, null);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    private User requireBuyer(HttpServletRequest req) throws AppException {
        User user = requireUser(req);
        if (user.getRole() != User.Role.BUYER) {
            throw new ForbiddenException("Only buyers have a wishlist");
        }
        return user;
    }
}
