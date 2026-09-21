package com.krishva.krishvamart.chat;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.model.Product;

@ExtendWith(MockitoExtension.class)
class CatalogAwareChatProviderTest {

    @Mock
    private ProductDAO productDAO;
    @Mock
    private ChatProvider delegate;   
    private CatalogAwareChatProvider provider;

    @BeforeEach
    public void setUp() {
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
        when(productDAO.search(eq("chair"), any(), eq(true)))
                .thenReturn(List.of(product("Chair", 50, "699.00")));
        String reply = provider.getReply("Is the chair in stock?", null);
        assertTrue(reply.contains("50 units available"));
        verify(delegate, org.mockito.Mockito.never()).getReply(any(), any());
    }

    @Test
    void stockQuestion_reportsOutOfStock() throws Exception {
        when(productDAO.search(eq("parker pen"), any(), eq(true)))
                .thenReturn(List.of(product("Parker Pen", 0, "349.00")));
        String reply = provider.getReply("is parker pen available", null);
        assertTrue(reply.toLowerCase().contains("out of stock"));
    }

    @Test
    void priceQuestion_answersFromLiveCatalog() throws Exception {
        when(productDAO.search(eq("adjustable dumbbell"), any(), eq(true)))
                .thenReturn(List.of(product("Adjustable Dumbbell", 30, "799.00")));
        String reply = provider.getReply("what's the price of adjustable dumbbell", null);
        assertTrue(reply.contains("799.00"));
    }

    @Test
    void brandQuestion_fallsThroughToDelegateWithProfessionalTone() throws Exception {
        when(delegate.getReply(any(), any()))
                .thenReturn("KrishvaMart - the most trusted e-commerce website.");
        String reply = provider.getReply("What is krishvamart", null);
        assertEquals("KrishvaMart - the most trusted e-commerce website.", reply);
    }

    @Test
    void categoryQuestion_fallsThroughToDelegateWithProfessionalTone() throws Exception {
        when(delegate.getReply(any(), any()))
                .thenReturn("KrishvaMart includes products for home, appliances, and electronics.");
        String reply = provider.getReply("what products do you sell", null);
        assertEquals("KrishvaMart includes products for home, appliances, and electronics.", reply);
    }

    @Test
    void returnsQuestion_fallsThroughToDelegateWithProfessionalTone() throws Exception {
        when(delegate.getReply(any(), any()))
                .thenReturn("KrishvaMart ensures a seamless experience with a 3-day hassle-free return policy.");
        String reply = provider.getReply("how do returns work", null);
        assertEquals("KrishvaMart ensures a seamless experience with a 3-day hassle-free return policy.", reply);
    }
}