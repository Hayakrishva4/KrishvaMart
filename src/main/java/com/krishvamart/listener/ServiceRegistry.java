package com.krishva.krishvamart.listener;

import com.krishva.krishvamart.chat.CatalogAwareChatProvider;
import com.krishva.krishvamart.chat.ChatProvider;
import com.krishva.krishvamart.chat.ChatService;
import com.krishva.krishvamart.chat.GeminiChatProvider;
import com.krishva.krishvamart.chat.MockChatProvider;
import com.krishva.krishvamart.dao.*;
import com.krishva.krishvamart.dao.impl.*;
import com.krishva.krishvamart.service.*;
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
    private final ChatService chatService;

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

        String providerFlag = config != null ? config.get("ai.chatbot.provider", "mock") : "mock";
        String apiKey = config != null ? config.get("ai.chatbot.api.key", "") : "";

        ChatProvider provider = ("gemini".equalsIgnoreCase(providerFlag) && !apiKey.isBlank())
                ? new GeminiChatProvider(apiKey)
                : new MockChatProvider();

        ChatProvider catalogAware = new CatalogAwareChatProvider(productDAO, provider);
        this.chatService = new ChatService(catalogAware);
    }

    public UserService userService() { return userService; }
    public ProductService productService() { return productService; }
    public CartService cartService() { return cartService; }
    public OrderService orderService() { return orderService; }
    public ReviewService reviewService() { return reviewService; }
    public WishlistService wishlistService() { return wishlistService; }
    public SellerAnalyticsService sellerAnalyticsService() { return sellerAnalyticsService; }
    public ChatService chatService() { return chatService; }
}