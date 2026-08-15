package com.krishva.krishvamart.chat;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogAwareChatProviderTest {

    @Mock
    private ProductDAO productDAO;
    @Mock
    private ChatProvider delegate;

    private CatalogAwareChatProvider provider;

    @BeforeEach
    void setUp() {
        provider = new CatalogAwareChatProvider(productDAO, delegate);
    }

    private Product product(String name, int stock, String price) {
        Product p = new Product();
        p.setName(name);
        p.setStockQty(stock);
        p.setPrice(new BigDecimal(price));
        p.setActive(true);
        return p;
    }

    @Test
    void stockQuestion_answersFromLiveCatalogWhenMatchFound() throws Exception {
        when(productDAO.search(eq("wireless mouse"), any(), eq(true)))
                .thenReturn(List.of(product("Wireless Mouse", 12, "699.00")));

        String reply = provider.getReply("Is the wireless mouse in stock?", null);

        assertTrue(reply.contains("12 units available"));
        verify(delegate, org.mockito.Mockito.never()).getReply(any(), any());
    }

    @Test
    void stockQuestion_reportsOutOfStock() throws Exception {
        when(productDAO.search(eq("keyboard"), any(), eq(true)))
                .thenReturn(List.of(product("Mechanical Keyboard", 0, "2499.00")));

        String reply = provider.getReply("is keyboard available", null);

        assertTrue(reply.contains("out of stock"));
    }

    @Test
    void priceQuestion_answersFromLiveCatalog() throws Exception {
        when(productDAO.search(eq("denim jacket"), any(), eq(true)))
                .thenReturn(List.of(product("Denim Jacket", 25, "1899.00")));

        String reply = provider.getReply("what's the price of denim jacket", null);

        assertTrue(reply.contains("1899.00"));
    }

    @Test
    void unrelatedQuestion_fallsThroughToDelegate() throws Exception {
        when(delegate.getReply("how do returns work", null)).thenReturn("Returns are accepted within 7 days.");

        String reply = provider.getReply("how do returns work", null);

        assertEquals("Returns are accepted within 7 days.", reply);
    }

    @Test
    void stockQuestionWithNoCatalogMatch_fallsThroughToDelegate() throws Exception {
        when(productDAO.search(eq("unobtanium widget"), any(), eq(true))).thenReturn(List.of());
        when(delegate.getReply(any(), any())).thenReturn("I can help with questions about products and orders.");

        String reply = provider.getReply("is unobtanium widget in stock", null);

        assertEquals("I can help with questions about products and orders.", reply);
    }
}
