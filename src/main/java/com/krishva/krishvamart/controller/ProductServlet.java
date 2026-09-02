package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.PagedResult;
import com.krishva.krishvamart.dto.ProductRequestDTO;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.IOException;
import java.math.BigDecimal;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/api/v1/products", "/api/v1/products/*"})
public class ProductServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String idPart = req.getPathInfo();
            String idParam = req.getParameter("id");

            if (idParam != null && !idParam.isBlank()) {
                long id = Long.parseLong(idParam.trim());
                Product product = services().productService().get(id);
                JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, product);
                return;
            }

            if (idPart != null && !idPart.isBlank() && !"/".equals(idPart)) {
                long id = parseId(idPart);
                Product product = services().productService().get(id);
                JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, product);
                return;
            }

            if ("true".equalsIgnoreCase(req.getParameter("sellerOnly"))) {
                User seller = requireSeller(req);
                JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK,
                        services().productService().listForSeller(seller.getId()));
                return;
            }

            PagedResult<Product> results = services().productService().search(buildCriteria(req));
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, results);
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
            if (body == null) {
                throw new ValidationException("body", "Request body is required");
            }
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
            if (body == null) {
                throw new ValidationException("body", "Request body is required");
            }
            Product updated = services().productService().update(id, seller.getId(), body.getName(), body.getDescription(),
                    body.getPrice(), body.getStockQty(), body.getCategory(), body.getImageUrl());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, updated);
        } catch (AppException e) {
            handleError(resp, e);
        } catch (NumberFormatException e) {
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
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Invalid product id");
        }
    }

    private ProductSearchCriteria buildCriteria(HttpServletRequest req) throws ValidationException {
        ProductSearchCriteria.Builder builder = ProductSearchCriteria.builder()
                .keyword(req.getParameter("q"))
                .category(req.getParameter("category"));

        String minPrice = req.getParameter("minPrice");
        if (minPrice != null && !minPrice.isBlank()) {
            builder.minPrice(parseMoney(minPrice, "minPrice"));
        }
        String maxPrice = req.getParameter("maxPrice");
        if (maxPrice != null && !maxPrice.isBlank()) {
            builder.maxPrice(parseMoney(maxPrice, "maxPrice"));
        }
        String sort = req.getParameter("sort");
        if (sort != null && !sort.isBlank()) {
            try {
                builder.sortBy(ProductSearchCriteria.SortBy.valueOf(sort.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("sort", "sort must be one of RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST");
            }
        }
        String page = req.getParameter("page");
        if (page != null && !page.isBlank()) {
            builder.page(parseIntParam(page, "page"));
        }
        String pageSize = req.getParameter("pageSize");
        if (pageSize != null && !pageSize.isBlank()) {
            builder.pageSize(parseIntParam(pageSize, "pageSize"));
        }
        return builder.build();
    }

    private BigDecimal parseMoney(String raw, String field) throws ValidationException {
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " must be a number");
        }
    }

    private int parseIntParam(String raw, String field) throws ValidationException {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " must be an integer");
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
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            throw new NotFoundException("Product id required");
        }
        return Long.parseLong(pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo);
    }
}