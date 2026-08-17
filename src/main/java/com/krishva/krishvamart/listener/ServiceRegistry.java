package com.krishva.krishvamart.listener;

import com.krishva.krishvamart.dao.AnalyticsDAO;
import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.dao.impl.JdbcAnalyticsDAO;
import com.krishva.krishvamart.dao.impl.JdbcCartDAO;
import com.krishva.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.service.CartService;
import com.krishva.krishvamart.service.OrderService;
import com.krishva.krishvamart.service.ProductService;
import com.krishva.krishvamart.service.SellerAnalyticsService;
import com.krishva.krishvamart.service.UserService;
import com.krishva.krishvamart.util.ConfigResolver;

// Review, Wishlist, Chat imports remain deferred (Weeks 5+)
// import com.krishva.krishvamart.dao.ReviewDAO;
// import com.krishva.krishvamart.dao.WishlistDAO;
// import com.krishva.krishvamart.dao.impl.JdbcReviewDAO;
// import com.krishva.krishvamart.dao.impl.JdbcWishlistDAO;
// import com.krishva.krishvamart.chat.ChatProvider;
// import com.krishva.krishvamart.chat.ChatService;
// import com.krishva.krishvamart.chat.GeminiChatProvider;
// import com.krishva.krishvamart.chat.MockChatProvider;
// import com.krishva.krishvamart.service.ReviewService;
// import com.krishva.krishvamart.service.WishlistService;

import javax.sql.DataSource;

public final class ServiceRegistry {

    public static final String ATTR = "krishvamart.services";

    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final SellerAnalyticsService sellerAnalyticsService;

    /* 
    private final ReviewService reviewService;
    private final ChatService chatService;
    private final WishlistService wishlistService;
    */

    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, ConfigResolver.load());
    }

    public ServiceRegistry(DataSource dataSource, ConfigResolver config) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        AnalyticsDAO analyticsDAO = new JdbcAnalyticsDAO(dataSource);

        // ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);
        // WishlistDAO wishlistDAO = new JdbcWishlistDAO(dataSource);

        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);
        this.sellerAnalyticsService = new SellerAnalyticsService(analyticsDAO);

        // this.reviewService = new ReviewService(reviewDAO, orderDAO);
        // this.wishlistService = new WishlistService(wishlistDAO, productDAO);
        
        /* 
        String providerFlag = config.get("ai.chatbot.provider", "mock");
        ChatProvider provider = "gemini".equalsIgnoreCase(providerFlag)
                ? new GeminiChatProvider(config.get("ai.chatbot.api.key", ""))
                : new MockChatProvider();
        ChatProvider catalogAware = new com.krishva.krishvamart.chat.CatalogAwareChatProvider(productDAO, provider);
        this.chatService = new ChatService(catalogAware);
        */
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

    public SellerAnalyticsService sellerAnalyticsService() {
        return sellerAnalyticsService;
    }

    /* 
    public ReviewService reviewService() {
        return reviewService;
    }

    public ChatService chatService() {
        return chatService;
    }

    public WishlistService wishlistService() {
        return wishlistService;
    }
    */
}