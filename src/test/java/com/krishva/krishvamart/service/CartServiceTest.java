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

import com.krishva.krishvamart.dao.CartDAO;
import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.CartItem;
import com.krishva.krishvamart.model.Product;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;
    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    public void setUp() {
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
        ValidationException ex = assertThrows(ValidationException.class, () -> cartService.addItem(1L, 1L, 0));
        assertNotNull(ex);
    }

    @Test
    void addItem_rejectsUnknownProduct() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () -> cartService.addItem(1L, 1L, 1));
        assertNotNull(ex);
    }

    @Test
    void addItem_rejectsInactiveProduct() throws Exception {
        Product inactive = activeProduct(5);
        inactive.setActive(false);
        when(productDAO.findById(1L)).thenReturn(Optional.of(inactive));
        ConflictException ex = assertThrows(ConflictException.class, () -> cartService.addItem(1L, 1L, 1));
        assertNotNull(ex);
    }

    @Test
    void addItem_rejectsQuantityExceedingStock() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.of(activeProduct(3)));
        when(cartDAO.findByUserAndProduct(1L, 1L)).thenReturn(Optional.empty());
        ConflictException ex = assertThrows(ConflictException.class, () -> cartService.addItem(1L, 1L, 5));
        assertNotNull(ex);
    }

    @Test
    void addItem_accumulatesExistingQuantityAgainstStockLimit() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.of(activeProduct(5)));
        CartItem existing = new CartItem();
        existing.setQuantity(4);
        when(cartDAO.findByUserAndProduct(1L, 1L)).thenReturn(Optional.of(existing));

        ConflictException ex = assertThrows(ConflictException.class, () -> cartService.addItem(1L, 1L, 2));
        assertNotNull(ex);
    }
}