package com.krishva.krishvamart.dao;

import java.math.BigDecimal;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.krishva.krishvamart.dao.impl.JdbcProductDAO;
import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.dto.PagedResult;
import com.krishva.krishvamart.dto.ProductSearchCriteria;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.User;
import com.zaxxer.hikari.HikariDataSource;

class JdbcProductDAOCriteriaSearchTest {

    private HikariDataSource dataSource;
    private ProductDAO productDAO;
    private long sellerId;

    @BeforeEach
    public void setUp() throws Exception {
        HikariDataSource ds = TestDataSource.create();
        this.dataSource = ds;
        this.productDAO = new JdbcProductDAO(ds);
        UserDAO userDAO = new JdbcUserDAO(ds);

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
    public void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
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
        ProductSearchCriteria firstPage = ProductSearchCriteria.builder().page(1).pageSize(2).build();
        ProductSearchCriteria secondPage = ProductSearchCriteria.builder().page(2).pageSize(2).build();

        PagedResult<Product> resultA = productDAO.search(firstPage);
        PagedResult<Product> resultB = productDAO.search(secondPage);

        assertEquals(2, resultA.getItems().size());
        assertEquals(2, resultB.getItems().size());
        assertEquals(4L, resultA.getTotalItems());
        assertEquals(2, resultA.getTotalPages());
        assertTrue(resultA.getItems().stream().noneMatch(a ->
                        resultB.getItems().stream().anyMatch(b -> Objects.equals(b.getId(), a.getId()))),
                "Page batches should not overlap");
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