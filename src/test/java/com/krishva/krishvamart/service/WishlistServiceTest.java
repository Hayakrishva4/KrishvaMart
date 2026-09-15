package com.krishva.krishvamart.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.dao.WishlistDAO;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.model.Product;
import com.krishva.krishvamart.model.WishlistItem;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistDAO wishlistDAO;
    @Mock
    private ProductDAO productDAO;

    private WishlistService wishlistService;

    @BeforeEach
    public void setUp() {
        wishlistService = new WishlistService(wishlistDAO, productDAO);
    }

    @Test
    void add_rejectsUnknownProduct() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () -> wishlistService.add(10L, 1L));
        assertNotNull(ex);
    }

    @Test
    void add_savesWhenProductExists() throws Exception {
        when(productDAO.findById(1L)).thenReturn(Optional.of(new Product()));
        WishlistItem expected = new WishlistItem();
        when(wishlistDAO.add(10L, 1L)).thenReturn(expected);

        WishlistItem result = wishlistService.add(10L, 1L);

        assertEquals(expected, result);
        verify(wishlistDAO).add(10L, 1L);
    }

    @Test
    void remove_throwsWhenNotInWishlist() throws Exception {
        when(wishlistDAO.remove(10L, 1L)).thenReturn(false);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> wishlistService.remove(10L, 1L));
        assertNotNull(ex);
    }
}