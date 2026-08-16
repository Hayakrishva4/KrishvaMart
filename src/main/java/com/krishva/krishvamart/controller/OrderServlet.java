package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.OrderStatusRequestDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OrderServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Order> orders = switch (user.getRole()) {
                    case BUYER -> services().orderService().historyForBuyer(user.getId());
                    case SELLER -> services().orderService().incomingForSeller(user.getId());
                    case ADMIN -> services().orderService().listAllForAdmin(user);
                };
                JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, orders);
                return;
            }
            long id = parseOrderId(pathInfo);
            Order order = services().orderService().get(id, user);
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, order);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid order id");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            if (user.getRole() != User.Role.BUYER) {
                throw new ForbiddenException("Only buyers can place orders");
            }
            if (!"/checkout".equals(req.getPathInfo())) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown route");
                return;
            }
            com.krishva.krishvamart.dto.CheckoutRequestDTO body =
                    readBody(req, com.krishva.krishvamart.dto.CheckoutRequestDTO.class);
            String shippingAddress = body == null ? null : body.getShippingAddress();
            Order order = services().orderService().checkout(user.getId(), true, shippingAddress);
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_CREATED,
                    com.krishva.krishvamart.dto.OrderConfirmationDTO.fromOrder(order));
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = requireUser(req);
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || !pathInfo.endsWith("/status")) {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown route");
                return;
            }
            long id = Long.parseLong(pathInfo.substring(1, pathInfo.indexOf("/status")));
            OrderStatusRequestDTO body = readBody(req, OrderStatusRequestDTO.class);
            Order.Status status = parseStatus(body == null ? null : body.getStatus());
            services().orderService().advanceStatus(id, status, user);
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "status", status.name()));
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid order id");
        }
    }

    private long parseOrderId(String pathInfo) throws NotFoundException {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/checkout")) {
            throw new NotFoundException("Order id required");
        }
        return Long.parseLong(pathInfo.substring(1));
    }

    private Order.Status parseStatus(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("status", "status is required");
        }
        try {
            return Order.Status.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("status", "Unknown status: " + raw);
        }
    }
}