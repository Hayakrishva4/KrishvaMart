package com.krishvamart.listener;

//import com.krishvamart.dao.CartDAO;
//import com.krishvamart.dao.OrderDAO;
import com.krishvamart.dao.ProductDAO;
//import com.krishvamart.dao.ReviewDAO;
import com.krishvamart.dao.UserDAO;
//import com.krishvamart.dao.impl.JdbcCartDAO;
//import com.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishvamart.dao.impl.JdbcProductDAO;
//import com.krishvamart.dao.impl.JdbcReviewDAO;
import com.krishvamart.dao.impl.JdbcUserDAO;
//import com.krishvamart.chat.ChatProvider;
//import com.krishvamart.chat.ChatService;
//import com.krishvamart.chat.GeminiChatProvider;
//import com.krishvamart.chat.MockChatProvider;
//import com.krishvamart.service.CartService;
//import com.krishvamart.service.OrderService;
//import com.krishvamart.exeception.ForbiddenException;
//import com.krishvamart.exeception.NotFoundException;
import com.krishvamart.service.ProductService;
//import com.krishvamart.service.ReviewService;
import com.krishvamart.service.UserService;

//import javax.sql.DataSource;
public final class ServiceRegistry {
    public static final String ATTR = "krishvamart.services";
    private final UserService userService;
    private final ProductService productService;

    // private final CartService cartService;
    // private final OrderService orderService;
    // private final ReviewService reviewService;
    // private final ChatService chatService;
    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, loadChatConfig());
    }

    public ServiceRegistry(DataSource dataSource, java.util.Properties chatConfig) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        // CartDAO cartDAO = new JdbcCartDAO(dataSource);
        // OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        // ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);
        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        // this.cartService = new CartService(cartDAO, productDAO);
        // this.orderService = new OrderService(dataSource, orderDAO, productDAO,
        // cartDAO);
        // this.reviewService = new ReviewService(reviewDAO, orderDAO);
        // String providerFlag = chatConfig.getProperty("ai.chatbot.provider", "mock");
        // ChatProvider provider = "gemini".equalsIgnoreCase(providerFlag)
        // ? new GeminiChatProvider(chatConfig.getProperty("ai.chatbot.apiKey", ""))
        // : new MockChatProvider();
        // this.chatService = new ChatService(provider);
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
    /*
     * public CartService cartService() {
     * return cartService;
     * }
     * public OrderService orderService() {
     * return orderService;
     * }
     * public ReviewService reviewService() {
     * return reviewService;
     * }
     * //public ChatService chatService() {
     * // return chatService;
     * //}
     */
}
