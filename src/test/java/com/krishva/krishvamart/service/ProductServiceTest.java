package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productDAO);
    }

    @Test
    void create_rejectsBlankName() {
        assertThrows(ValidationException.class,
                () -> productService.create(1L, "", "desc", new BigDecimal("10.00"), 5, "Tools", null));
    }

    @Test
    void create_rejectsNonPositivePrice() {
        assertThrows(ValidationException.class,
                () -> productService.create(1L, "Widget", "desc", BigDecimal.ZERO, 5, "Tools", null));
    }

    @Test
    void create_rejectsNegativeStock() {
        assertThrows(ValidationException.class,
                () -> productService.create(1L, "Widget", "desc", new BigDecimal("10.00"), -1, "Tools", null));
    }

    @Test
    void create_rejectsBlankCategory() {
        assertThrows(ValidationException.class,
                () -> productService.create(1L, "Widget", "desc", new BigDecimal("10.00"), 5, "", null));
    }

    @Test
    void update_rejectsNonOwningSeller() throws Exception {
        Product existing = new Product();
        existing.setId(1L);
        existing.setSellerId(99L);
        when(productDAO.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenException.class,
                () -> productService.update(1L, 1L, "New name", "desc", new BigDecimal("15.00"), 5, "Tools", null));
    }

    @Test
    void delete_rejectsNonOwningSeller() throws Exception {
        Product existing = new Product();
        existing.setId(1L);
        existing.setSellerId(99L);
        when(productDAO.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenException.class, () -> productService.delete(1L, 1L));
    }
}
