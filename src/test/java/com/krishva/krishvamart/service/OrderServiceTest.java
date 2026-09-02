package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.OrderDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.TestDataSource;
import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.dao.impl.JdbcCartDAO;
import com.krishva.krishvamart.dao.impl.JdbcOrderDAO;
import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Order;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {
    private HikariDataSource dataSource;
    private OrderService orderService;
    private CartService cartService;
    private ProductDAO productDAO;
    private long buyerId;
    private long productId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = TestDataSource.create();
        UserDAO userDAO = new JdbcUserDAO(dataSource);
        productDAO = new JdbcProductDAO(dataSource);
        CartDAO cartDAO = new JdbcCartDAO(dataSource);
        OrderDAO orderDAO = new JdbcOrderDAO(dataSource);
        orderService = new OrderService(dataSource, orderDAO, productDAO, cartDAO);
        cartService = new CartService(cartDAO, productDAO);
        User seller = new User();
        seller.setName("Seller");
        seller.setEmail("seller@example.com");
        seller.setPasswordHash("x");
        seller.setRole(User.Role.SELLER);
        long sellerId = userDAO.insert(seller).getId();
        User buyer = new User();
        buyer.setName("Buyer");
        buyer.setEmail("buyer@example.com");
        buyer.setPasswordHash("x");
        buyer.setRole(User.Role.BUYER);
        buyerId = userDAO.insert(buyer).getId();
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName("Widget");
        product.setDescription("desc");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQty(5);
        product.setCategory("Tools");
        productId = productDAO.insert(product).getId();
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    @Test
    void checkout_rejectsEmptyCart() {
        assertThrows(ValidationException.class, () -> orderService.checkout(buyerId, true, "123 Main St, Chennai, TN 600001"));
    }

    @Test
    void checkout_rejectsWithoutMockPaymentConfirmation() {
        assertThrows(ValidationException.class, () -> orderService.checkout(buyerId, false, "123 Main St, Chennai, TN 600001"));
    }

    @Test
    void checkout_rejectsBlankShippingAddress() throws Exception {
        cartService.addItem(buyerId, productId, 1);
        assertThrows(ValidationException.class, () -> orderService.checkout(buyerId, true, "  "));
    }

    @Test
    void checkout_decrementsStockAndClearsCartOnSuccess() throws Exception {
        cartService.addItem(buyerId, productId, 3);
        Order order = orderService.checkout(buyerId, true, "123 Main St, Chennai, TN 600001");
        assertEquals(Order.Status.CONFIRMED, order.getStatus());
        assertEquals(0, new BigDecimal("30.00").compareTo(order.getTotalAmount()));
        assertEquals(1, order.getItems().size());
        Product afterCheckout = productDAO.findById(productId).orElseThrow();
        assertEquals(2, afterCheckout.getStockQty(), "Stock should be decremented by the ordered quantity");
        assertTrue(cartService.view(buyerId).isEmpty(), "Cart should be cleared after checkout");
    }

    @Test
    void checkout_rollsBackEverythingWhenStockRaceLeavesInsufficientStock() throws Exception {
        cartService.addItem(buyerId, productId, 3);
        productDAO.adjustStock(productId, -3);
        assertEquals(2, productDAO.findById(productId).orElseThrow().getStockQty());
        assertThrows(ConflictException.class, () -> orderService.checkout(buyerId, true, "123 Main St, Chennai, TN 600001"));
        assertEquals(2, productDAO.findById(productId).orElseThrow().getStockQty());
        assertEquals(1, cartService.view(buyerId).size(), "Cart should not be cleared when checkout rolls back");
    }
}
