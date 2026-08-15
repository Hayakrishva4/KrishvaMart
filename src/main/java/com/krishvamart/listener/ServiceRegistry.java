package com.krishvamart.listener;

import com.krishvamart.chat.CatalogAwareChatProvider;
import com.krishvamart.chat.ChatProvider;
import com.krishvamart.chat.ChatService;
import com.krishvamart.chat.GeminiChatProvider;
import com.krishvamart.chat.MockChatProvider;
import com.krishvamart.dao.AnalyticsDAO;
import com.krishvamart.dao.CartDAO;
import com.krishvamart.dao.OrderDAO;
import com.krishvamart.dao.ProductDAO;
import com.krishvamart.dao.ReviewDAO;
import com.krishvamart.dao.UserDAO;
import com.krishvamart.dao.WishlistDAO;
import com.krishvamart.dao.impl.JdbcAnalyticsDAO;
import com.krishvamart.dao.impl.JdbcCartDAO;
import com.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishvamart.dao.impl.JdbcProductDAO;
import com.krishvamart.dao.impl.JdbcReviewDAO;
import com.krishvamart.dao.impl.JdbcUserDAO;
import com.krishvamart.dao.impl.JdbcWishlistDAO;
import com.krishvamart.service.CartService;
import com.krishvamart.service.OrderService;
import com.krishvamart.service.ProductService;
import com.krishvamart.service.ReviewService;
import com.krishvamart.service.SellerAnalyticsService;
import com.krishvamart.service.UserService;
import com.krishvamart.service.WishlistService;
import com.krishvamart.util.ConfigResolver;

import javax.sql.DataSource;

/**
 * ServiceRegistry - Hand-rolled DI container wiring DAOs into core and
 * advanced milestone services, including catalog-aware AI chat.
 */
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
        // 1. Initialize DAOs
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);
        WishlistDAO wishlistDAO = new JdbcWishlistDAO(dataSource);
        AnalyticsDAO analyticsDAO = new JdbcAnalyticsDAO(dataSource);

        // 2. Wire Core Services (Weeks 1 & 2)
        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);

        // 3. Wire Advanced Services (Week 3)
        this.reviewService = new ReviewService(reviewDAO, orderDAO);
        this.wishlistService = new WishlistService(wishlistDAO, productDAO);
        this.sellerAnalyticsService = new SellerAnalyticsService(analyticsDAO);

        // 4. Wire AI Chatbot Provider & Decorator (Week 3)
        String providerFlag = config != null ? config.get("ai.chatbot.provider", "mock") : "mock";
        String apiKey = config != null ? config.get("ai.chatbot.api.key", "") : "";

        ChatProvider provider = ("gemini".equalsIgnoreCase(providerFlag) && !apiKey.isBlank())
                ? new GeminiChatProvider(apiKey)
                : new MockChatProvider();

        // Catalog-aware decorator answers product stock/pricing queries directly
        ChatProvider catalogAware = new CatalogAwareChatProvider(productDAO, provider);
        this.chatService = new ChatService(catalogAware);
    }

    // --- Service Getters ---
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

    public ChatService chatService() {
        return chatService;
    }
}