package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.CartItemRequestDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.CartItem;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CartServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            List<CartItem> items = services().cartService().view(user.getId());
            BigDecimal total = services().cartService().runningTotal(user.getId());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, Map.of("items", items, "total", total));
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            CartItemRequestDTO body = readBody(req, CartItemRequestDTO.class);
            if (body == null || body.getProductId() == null || body.getQuantity() == null) {
                throw new ValidationException("productId", "productId and quantity are required");
            }
            CartItem item = services().cartService().addItem(user.getId(), body.getProductId(), body.getQuantity());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_CREATED, item);
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            long productId = parseProductId(req.getPathInfo());
            CartItemRequestDTO body = readBody(req, CartItemRequestDTO.class);
            if (body == null || body.getQuantity() == null) {
                throw new ValidationException("quantity", "quantity is required");
            }
            services().cartService().updateQuantity(user.getId(), productId, body.getQuantity());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, null);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            long productId = parseProductId(req.getPathInfo());
            services().cartService().removeItem(user.getId(), productId);
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, null);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    private long parseProductId(String pathInfo) throws NotFoundException {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new NotFoundException("Product id required in path");
        }
        return Long.parseLong(pathInfo.substring(1));
    }
}