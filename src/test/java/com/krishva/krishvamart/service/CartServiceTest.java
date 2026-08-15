package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.CartItem;
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
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;
    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartDAO, productDAO);
    }

    private Product activeProduct(int stock) {
        Product p = new Product();
        p.setId(1L);
        p.setActive(true);
        p.setStockQty(stock);
        p.setPrice(new BigDecimal("10.00"));
        return p;
    }

    @Test
    void addItem_rejectsNonPositiveQuantity() {
        assertThrows(ValidationException.class, () -> cartService.addItem(1L, 1L, 0));
    }

    @Test
    void addItem_rejectsUnknownProduct() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cartService.addItem(1L, 1L, 1));
    }

    @Test
    void addItem_rejectsInactiveProduct() throws Exception {
        Product inactive = activeProduct(5);
        inactive.setActive(false);
        when(productDAO.findById(1L)).thenReturn(Optional.of(inactive));
        assertThrows(ConflictException.class, () -> cartService.addItem(1L, 1L, 1));
    }

    @Test
    void addItem_rejectsQuantityExceedingStock() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.of(activeProduct(3)));
        when(cartDAO.findByUserAndProduct(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(ConflictException.class, () -> cartService.addItem(1L, 1L, 5));
    }

    @Test
    void addItem_accumulatesExistingQuantityAgainstStockLimit() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.of(activeProduct(5)));
        CartItem existing = new CartItem();
        existing.setQuantity(4);
        when(cartDAO.findByUserAndProduct(1L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> cartService.addItem(1L, 1L, 2));
    }
}
