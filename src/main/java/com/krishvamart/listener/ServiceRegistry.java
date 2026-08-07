package com.krishva.krishvamart.listener;
import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.ReviewDAO;
import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.dao.impl.JdbcCartDAO;
import com.krishva.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcReviewDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.chat.ChatProvider;
import com.krishva.krishvamart.chat.ChatService;
import com.krishva.krishvamart.chat.GeminiChatProvider;
import com.krishva.krishvamart.chat.MockChatProvider;
import com.krishva.krishvamart.service.CartService;
import com.krishva.krishvamart.service.OrderService;
import com.krishva.krishvamart.service.ProductService;
import com.krishva.krishvamart.service.ReviewService;
import com.krishva.krishvamart.service.UserService;
import javax.sql.DataSource;
public final class ServiceRegistry {
    public static final String ATTR = "krishvamart.services";
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ChatService chatService;
    public ServiceRegistry(DataSource dataSource) {
        this(dataSource, loadChatConfig());
    }
    public ServiceRegistry(DataSource dataSource, java.util.Properties chatConfig) {
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        ProductDAO productDAO = new JdbcProductDAO(dataSource);
        UCartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        ReviewDAO reviewDAO = new JdbcReviewDAO(dataSource);
        this.userService = new UserService(userDAO);
        this.productService = new ProductService(productDAO);
        this.cartService = new CartService(cartDAO, productDAO);
        this.orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);
        this.reviewService = new ReviewService(reviewDAO, orderDAO);
        String providerFlag = chatConfig.getProperty("ai.chatbot.provider", "mock");
        ChatProvider provider = "gemini".equalsIgnoreCase(providerFlag)
                ? new GeminiChatProvider(chatConfig.getProperty("ai.chatbot.apiKey", ""))
                : new MockChatProvider();
        this.chatService = new ChatService(provider);
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
    public ReviewService reviewService() {
        return reviewService;
    }
    public ChatService chatService() {
        return chatService;
    }
}
