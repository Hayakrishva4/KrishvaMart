package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcProductDAOTest {

    private HikariDataSource dataSource;
    private ProductDAO productDAO;
    private long sellerId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = TestDataSource.create();
        productDAO = new JdbcProductDAO(dataSource);
        UserDAO userDAO = new JdbcUserDAO(dataSource);

        User seller = new User();
        seller.setName("Seller One");
        seller.setEmail("seller@example.com");
        seller.setPasswordHash("x");
        seller.setRole(User.Role.SELLER);
        sellerId = userDAO.insert(seller).getId();
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    private Product sampleProduct(String name, String category) {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription("desc");
        p.setPrice(new BigDecimal("19.99"));
        p.setStockQty(10);
        p.setCategory(category);
        return p;
    }

    @Test
    void insert_thenFindById_returnsSavedProduct() throws Exception {
        Product saved = productDAO.insert(sampleProduct("Widget", "Tools"));
        assertTrue(saved.getId() > 0);
        assertTrue(productDAO.findById(saved.getId()).isPresent());
    }

    @Test
    void search_filtersByKeywordAndCategory() throws Exception {
        productDAO.insert(sampleProduct("Blue Widget", "Tools"));
        productDAO.insert(sampleProduct("Red Gadget", "Electronics"));

        List<Product> byKeyword = productDAO.search("widget", null, true);
        assertEquals(1, byKeyword.size());
        assertEquals("Blue Widget", byKeyword.get(0).getName());

        List<Product> byCategory = productDAO.search(null, "Electronics", true);
        assertEquals(1, byCategory.size());
    }

    @Test
    void adjustStock_decrementsAndRejectsOverdraw() throws Exception {
        Product saved = productDAO.insert(sampleProduct("Widget", "Tools"));

        boolean ok = productDAO.adjustStock(saved.getId(), -4);
        assertTrue(ok);
        assertEquals(6, productDAO.findById(saved.getId()).get().getStockQty());

        boolean overdraw = productDAO.adjustStock(saved.getId(), -100);
        assertFalse(overdraw, "Stock should never go negative");
        assertEquals(6, productDAO.findById(saved.getId()).get().getStockQty());
    }

    @Test
    void update_onlySucceedsForOwningSeller() throws Exception {
        Product saved = productDAO.insert(sampleProduct("Widget", "Tools"));
        saved.setName("Updated Widget");
        saved.setPrice(new BigDecimal("29.99"));
        saved.setStockQty(5);

        assertTrue(productDAO.update(saved));
        assertEquals("Updated Widget", productDAO.findById(saved.getId()).get().getName());

        saved.setSellerId(999L);
        assertFalse(productDAO.update(saved), "Update should fail for a non-owning seller id");
    }
}
