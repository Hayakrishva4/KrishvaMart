package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.dto.PagedResult;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcProductDAOCriteriaSearchTest {

    private HikariDataSource dataSource;
    private ProductDAO productDAO;
    private long sellerId;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = TestDataSource.create();
        productDAO = new JdbcProductDAO(dataSource);
        UserDAO userDAO = new JdbcUserDAO(dataSource);

        User seller = new User();
        seller.setName("Seller");
        seller.setEmail("seller@example.com");
        seller.setPasswordHash("x");
        seller.setRole(User.Role.SELLER);
        sellerId = userDAO.insert(seller).getId();

        insert("Cheap Widget", "10.00", 20, "Tools");
        insert("Mid Widget", "25.00", 20, "Tools");
        insert("Expensive Widget", "50.00", 20, "Tools");
        insert("Gadget", "15.00", 20, "Electronics");
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    private void insert(String name, String price, int stock, String category) throws Exception {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription("desc");
        p.setPrice(new BigDecimal(price));
        p.setStockQty(stock);
        p.setCategory(category);
        productDAO.insert(p);
    }

    @Test
    void search_filtersByPriceRange() throws Exception {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .minPrice(new BigDecimal("12.00"))
                .maxPrice(new BigDecimal("30.00"))
                .build();

        PagedResult<Product> result = productDAO.search(criteria);

        assertEquals(2, result.getItems().size(), "Mid Widget and Gadget fall in [12,30]");
        assertTrue(result.getItems().stream().allMatch(p ->
                p.getPrice().compareTo(new BigDecimal("12.00")) >= 0
                        && p.getPrice().compareTo(new BigDecimal("30.00")) <= 0));
    }

    @Test
    void search_sortsByPriceAscending() throws Exception {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .sortBy(ProductSearchCriteria.SortBy.PRICE_ASC)
                .pageSize(10)
                .build();

        PagedResult<Product> result = productDAO.search(criteria);

        assertEquals("Cheap Widget", result.getItems().get(0).getName());
        assertEquals("Expensive Widget", result.getItems().get(result.getItems().size() - 1).getName());
    }

    @Test
    void search_paginatesResults() throws Exception {
        ProductSearchCriteria page1 = ProductSearchCriteria.builder().page(1).pageSize(2).build();
        ProductSearchCriteria page2 = ProductSearchCriteria.builder().page(2).pageSize(2).build();

        PagedResult<Product> result1 = productDAO.search(page1);
        PagedResult<Product> result2 = productDAO.search(page2);

        assertEquals(2, result1.getItems().size());
        assertEquals(2, result2.getItems().size());
        assertEquals(4, result1.getTotalItems());
        assertEquals(2, result1.getTotalPages());
        assertTrue(result1.getItems().stream().noneMatch(a ->
                        result2.getItems().stream().anyMatch(b -> b.getId().equals(a.getId()))),
                "Page 1 and page 2 should not overlap");
    }

    @Test
    void search_combinesCategoryAndKeyword() throws Exception {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword("widget")
                .category("Tools")
                .pageSize(10)
                .build();

        PagedResult<Product> result = productDAO.search(criteria);

        assertEquals(3, result.getItems().size());
    }
}
