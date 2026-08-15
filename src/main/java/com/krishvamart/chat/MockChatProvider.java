package com.krishvamart.chat;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Canned FAQ answers, no network call. Default provider (ai.chatbot.provider=mock)
 * and the fallback used by ChatService when the real provider call fails
 * (Section 11, Rule 3: return a static degraded response instead of an error page).
 */
public class MockChatProvider implements ChatProvider {

    private static final Map<String, String> FAQ = new LinkedHashMap<>();

    static {
        FAQ.put("shipping", "Orders are typically dispatched within 2 business days of a CONFIRMED status.");
        FAQ.put("return", "Items can be returned within 7 days of delivery if unused and in original packaging.");
        FAQ.put("payment", "Checkout uses a mock payment confirmation step - no real card or bank details are collected.");
        FAQ.put("track", "You can track an order's status (Pending, Confirmed, Shipped, Delivered) from your Order History page.");
        FAQ.put("seller", "To sell on KrishvaMart, register with the Seller role and add listings from your seller dashboard.");
        FAQ.put("review", "You can leave a star rating and review once an order's status is Delivered.");
        FAQ.put("cancel", "Orders can be cancelled while still Pending or Confirmed; once Shipped they cannot be cancelled.");
        FAQ.put("stock", "Product stock updates in real time as orders are placed; out-of-stock items cannot be added to cart.");
    }

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null) {
            return defaultReply();
        }
        String lower = userMessage.toLowerCase();
        for (Map.Entry<String, String> entry : FAQ.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultReply();
    }

    private String defaultReply() {
        return "I can help with questions about products, orders, shipping, returns, and reviews on KrishvaMart. "
                + "Could you rephrase your question?";
    }
}
