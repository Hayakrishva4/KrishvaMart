package com.krishva.krishvamart.listener;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.dao.impl.JdbcCartDAO;
import com.krishva.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.service.CartService;
import com.krishva.krishvamart.service.OrderService;
import com.krishva.krishvamart.service.ProductService;
import com.krishva.krishvamart.service.UserService;
import javax.sql.DataSource;

public final class ServiceRegistry {
    public static final String ATTR = "krishvamart.services";
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;

    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, loadChatConfig());
    }

    public ServiceRegistry(DataSource dataSource, java.util.Properties chatConfig) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(orderDAO, cartDAO, productDAO);
    }

    private static java.util.Properties loadChatConfig() {
        java.util.Properties props = new java.util.Properties();
        try (var in = ServiceRegistry.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (java.io.IOException ignored) {
        }
        return props;
    }

    public UserService userService() {
        return userService;
    }

    public ProductService productService() {
        return productService;
    }

    public CartService cartService() {
        return cartService;
    }

    public OrderService orderService() {
        return orderService;
    }
}