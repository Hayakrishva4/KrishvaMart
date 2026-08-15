package com.krishva.krishvamart.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Real LLM-backed provider. The API key is read server-side only from
 * config.properties / environment (Section 11, Rule 1) and never appears in
 * client-side code - the browser only ever talks to ChatServlet.
 *
 * Swap the model/endpoint below for whichever provider your faculty guide
 * approves; the fixed server-side prompt template (Section 17, Rule 3)
 * restricts answers to the product/listing domain (Section 11, Rule 2).
 */
public class GeminiChatProvider implements ChatProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GeminiChatProvider.class);
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final String PROMPT_TEMPLATE = """
            You are the KrishvaMart support assistant. Answer ONLY questions about \
            products, orders, shipping, returns, payments (mock confirmation only, \
            no real payment gateway), and reviews on this marketplace. If asked \
            anything outside that scope, politely say you can only help with \
            KrishvaMart shopping questions. Keep answers under 3 sentences.

            Store context: %s

            Customer question: %s
            """;

    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiChatProvider(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getReply(String userMessage, String context) {
        if (apiKey == null || apiKey.isBlank()) {
            LOG.warn("Gemini provider selected but no API key configured");
            throw new IllegalStateException("AI provider not configured");
        }
        try {
            String prompt = PROMPT_TEMPLATE.formatted(context == null ? "general marketplace" : context, userMessage);

            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            JsonArray parts = new JsonArray();
            parts.add(part);
            JsonObject content = new JsonObject();
            content.add("parts", parts);
            JsonArray contents = new JsonArray();
            contents.add(content);
            JsonObject body = new JsonObject();
            body.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.error("Gemini API returned status {}: {}", response.statusCode(), response.body());
                throw new IllegalStateException("AI provider call failed");
            }
            return extractReply(response.body());
        } catch (Exception e) {
            // Wrapped in try/catch per Section 11, Rule 3; ChatService falls back
            // to MockChatProvider's static degraded response on any exception here.
            LOG.error("Gemini API call failed", e);
            throw new RuntimeException("AI provider call failed", e);
        }
    }

    private String extractReply(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        return root.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString()
                .trim();
    }
}
