package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.ProductRequestDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * F2: seller create/edit/delete listings. F3: buyer browse/search by
 * category and keyword (public, no login required - see AuthFilter).
 * Mapped at /api/v1/products and /api/v1/products/{id}.
 */
@WebServlet(urlPatterns = {"/api/v1/products", "/api/v1/products/*"})
public class ProductServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String idPart = req.getPathInfo();
            if (idPart == null || idPart.equals("/")) {
                if ("true".equalsIgnoreCase(req.getParameter("sellerOnly"))) {
                    User seller = requireSeller(req);
                    JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK,
                            services().productService().listForSeller(seller.getId()));
                    return;
                }
                String keyword = req.getParameter("q");
                String category = req.getParameter("category");
                List<Product> results = services().productService().search(keyword, category);
                JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, results);
            } else {
                long id = parseId(idPart);
                Product product = services().productService().get(id);
                JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, product);
            }
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User seller = requireSeller(req);
            ProductRequestDTO body = readBody(req, ProductRequestDTO.class);
            Product created = services().productService().create(seller.getId(), body.getName(), body.getDescription(),
                    body.getPrice(), body.getStockQty(), body.getCategory(), body.getImageUrl());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_CREATED, created);
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User seller = requireSeller(req);
            long id = parseId(req.getPathInfo());
            ProductRequestDTO body = readBody(req, ProductRequestDTO.class);
            Product updated = services().productService().update(id, seller.getId(), body.getName(), body.getDescription(),
                    body.getPrice(), body.getStockQty(), body.getCategory(), body.getImageUrl());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, updated);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException | NullPointerException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User seller = requireSeller(req);
            long id = parseId(req.getPathInfo());
            services().productService().delete(id, seller.getId());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, null);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException | NullPointerException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    private User requireSeller(HttpServletRequest req) throws AppException {
        User user = requireUser(req);
        if (user.getRole() != User.Role.SELLER) {
            throw new ForbiddenException("Only sellers can manage listings");
        }
        return user;
    }

    private long parseId(String pathInfo) throws NotFoundException {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new NotFoundException("Product id required");
        }
        return Long.parseLong(pathInfo.substring(1));
    }
}
