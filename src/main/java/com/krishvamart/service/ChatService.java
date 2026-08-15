package com.krishva.krishvamart.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class ChatService {
    private static final Logger LOG = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int RATE_LIMIT_PER_MINUTE = 10;
    private static final long RATE_WINDOW_MILLIS = 60_000L;
    private final ChatProvider provider;
    private final ChatProvider fallback = new MockChatProvider();
    private final Map<String, RateWindow> rateWindows = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> replyCache = new ConcurrentHashMap<>();
    public ChatService(ChatProvider provider) {
        this.provider = provider;
    }
    public String reply(String sessionId, String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Please type a question about products, orders, or shipping.";
        }
        String trimmed = userMessage.length() > MAX_MESSAGE_LENGTH
                ? userMessage.substring(0, MAX_MESSAGE_LENGTH)
                : userMessage;
        if (isRateLimited(sessionId)) {
            return "You're sending messages too quickly - please wait a moment and try again.";
        }
        Map<String, String> sessionCache = replyCache.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
        String cacheKey = trimmed.trim().toLowerCase();
        String cached = sessionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        String reply;
        try {
            reply = provider.getReply(trimmed, "KrishvaMart multi-seller marketplace");
        } catch (Exception e) {
            LOG.warn("Chat provider failed, using fallback: {}", e.getMessage());
            reply = fallback.getReply(trimmed, null);
        }
        sessionCache.put(cacheKey, reply);
        return reply;
    }
    private boolean isRateLimited(String sessionId) {
        long now = System.currentTimeMillis();
        RateWindow window = rateWindows.computeIfAbsent(sessionId, k -> new RateWindow());
        synchronized (window) {
            if (now - window.windowStart > RATE_WINDOW_MILLIS) {
                window.windowStart = now;
                window.count = 0;
            }
            window.count++;
            return window.count > RATE_LIMIT_PER_MINUTE;
        }
    }
    private static final class RateWindow {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}
