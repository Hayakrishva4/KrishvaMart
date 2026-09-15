package com.krishva.krishvamart.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Product;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    public void setUp() {
        productService = new ProductService(productDAO);
    }

    @Test
    void create_rejectsBlankName() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> productService.create(1L, "", "desc", new BigDecimal("10.00"), 5, "Tools", null));
        assertNotNull(ex);
    }

    @Test
    void create_rejectsNonPositivePrice() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> productService.create(1L, "Widget", "desc", BigDecimal.ZERO, 5, "Tools", null));
        assertNotNull(ex);
    }

    @Test
    void create_rejectsNegativeStock() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> productService.create(1L, "Widget", "desc", new BigDecimal("10.00"), -1, "Tools", null));
        assertNotNull(ex);
    }

    @Test
    void create_rejectsBlankCategory() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> productService.create(1L, "Widget", "desc", new BigDecimal("10.00"), 5, "", null));
        assertNotNull(ex);
    }

    @Test
    void update_rejectsNonOwningSeller() throws Exception {
        Product existing = new Product();
        existing.setId(1L);
        existing.setSellerId(99L);
        when(productDAO.findById(1L)).thenReturn(Optional.of(existing));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> productService.update(1L, 1L, "New name", "desc", new BigDecimal("15.00"), 5, "Tools", null));
        assertNotNull(ex);
    }

    @Test
    void delete_rejectsNonOwningSeller() throws Exception {
        Product existing = new Product();
        existing.setId(1L);
        existing.setSellerId(99L);
        when(productDAO.findById(1L)).thenReturn(Optional.of(existing));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> productService.delete(1L, 1L));
        assertNotNull(ex);
    }
}