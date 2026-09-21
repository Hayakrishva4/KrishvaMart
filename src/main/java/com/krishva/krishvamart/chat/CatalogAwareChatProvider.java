package com.krishva.krishvamart.chat;

import com.krishva.krishvamart.dao.ProductDAO;
import com.krishva.krishvamart.model.Product;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator over any {@link ChatProvider} that intercepts stock/price
 * questions and answers them from the live catalog (real-time DB lookup)
 * instead of sending them to the LLM/canned-FAQ layer. Section 11 only
 * requires "5-10 FAQ-style domain questions" answered from a fixed script;
 * this goes further by grounding a subset of answers in actual product data,
 * so "is the wireless mouse in stock" gets today's real stock_qty rather
 * than a generic canned reply.
 *
 * Falls through to the wrapped provider for anything it doesn't recognize,
 * so it composes cleanly with either {@link MockChatProvider} or
 * {@link GeminiChatProvider} (Strategy pattern, Section 12) without either
 * of them needing to know about the catalog.
 */
public class CatalogAwareChatProvider implements ChatProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogAwareChatProvider.class);

    private static final Pattern STOCK_QUESTION = Pattern.compile(
            "(?:is|are)\\s+(?:the\\s+|a\\s+)?(.+?)\\s+(?:in\\s+stock|available)\\??", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_QUESTION = Pattern.compile(
            "(?:price|cost|how much(?:\\s+is|\\s+does)?)\\s+(?:of|for|is)?\\s*(?:the\\s+|a\\s+)?(.+?)\\??$",
            Pattern.CASE_INSENSITIVE);

    private final ProductDAO productDAO;
    private final ChatProvider delegate;

    public CatalogAwareChatProvider(ProductDAO productDAO, ChatProvider delegate) {
        this.productDAO = productDAO;
        this.delegate = delegate;
    }

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null) {
            return delegate.getReply(userMessage, context);
        }
        String trimmed = userMessage.trim();

        Matcher stockMatch = STOCK_QUESTION.matcher(trimmed);
        if (stockMatch.find()) {
            String answer = tryAnswerStock(stockMatch.group(1));
            if (answer != null) {
                return answer;
            }
        }

        Matcher priceMatch = PRICE_QUESTION.matcher(trimmed);
        if (priceMatch.find()) {
            String answer = tryAnswerPrice(priceMatch.group(1));
            if (answer != null) {
                return answer;
            }
        }

        return delegate.getReply(userMessage, context);
    }

    private String tryAnswerStock(String rawProductName) {
        Product product = findBestMatch(rawProductName);
        if (product == null) {
            return null;
        }
        if (!product.isActive()) {
            return "\"" + product.getName() + "\" is no longer listed.";
        }
        return product.getStockQty() > 0
                ? "\"" + product.getName() + "\" is in stock - " + product.getStockQty() + " units available right now."
                : "\"" + product.getName() + "\" is currently out of stock.";
    }

    private String tryAnswerPrice(String rawProductName) {
        Product product = findBestMatch(rawProductName);
        if (product == null) {
            return null;
        }
        return "\"" + product.getName() + "\" is currently priced at $" + product.getPrice() + ".";
    }

    private Product findBestMatch(String rawProductName) {
        String keyword = rawProductName == null ? "" : rawProductName.trim();
        if (keyword.isEmpty()) {
            return null;
        }
        try {
            List<Product> matches = productDAO.search(keyword, null, true);
            return matches.isEmpty() ? null : matches.get(0);
        } catch (Exception e) {
            // Catalog lookup is a best-effort enhancement - any failure here
            // (including a DataAccessException) just falls through to the
            // wrapped provider rather than breaking the chat response.
            LOG.warn("Catalog lookup failed for chat query '{}': {}", keyword, e.getMessage());
            return null;
        }
    }
}
