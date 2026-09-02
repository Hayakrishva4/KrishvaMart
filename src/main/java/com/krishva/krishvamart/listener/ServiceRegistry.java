package com.krishva.krishvamart.listener;

import com.krishva.krishvamart.dao.AnalyticsDAO;
import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.ReviewDAO;
import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.dao.WishlistDAO;
import com.krishva.krishvamart.dao.impl.JdbcAnalyticsDAO;
import com.krishva.krishvamart.dao.impl.JdbcCartDAO;
import com.krishva.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcReviewDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.dao.impl.JdbcWishlistDAO;
import com.krishva.krishvamart.service.CartService;
import com.krishva.krishvamart.service.OrderService;
import com.krishva.krishvamart.service.ProductService;
import com.krishva.krishvamart.service.ReviewService;
import com.krishva.krishvamart.service.SellerAnalyticsService;
import com.krishva.krishvamart.service.UserService;
import com.krishva.krishvamart.service.WishlistService;
import com.krishva.krishvamart.util.ConfigResolver;
import javax.sql.DataSource;

public final class ServiceRegistry {

    public static final String ATTR = "krishvamart.services";

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final SellerAnalyticsService sellerAnalyticsService;

    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, ConfigResolver.load());
    }

    public ServiceRegistry(DataSource dataSource, ConfigResolver config) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);
        WishlistDAO wishlistDAO = new JdbcWishlistDAO(dataSource);
        AnalyticsDAO analyticsDAO = new JdbcAnalyticsDAO(dataSource);

        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);
        this.reviewService = new ReviewService(reviewDAO, orderDAO);
        this.wishlistService = new WishlistService(wishlistDAO, productDAO);
        this.sellerAnalyticsService = new SellerAnalyticsService(analyticsDAO);
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

    public ReviewService reviewService() {
        return reviewService;
    }

    public WishlistService wishlistService() {
        return wishlistService;
    }

    public SellerAnalyticsService sellerAnalyticsService() {
        return sellerAnalyticsService;
    }
}