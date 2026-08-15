package com.krishva.krishvamart.chat;

/**
 * Section 17, Rule 1: the AI provider is accessed through an interface, not
 * a hardcoded implementation, so the concrete provider can be swapped via
 * the ai.chatbot.provider config flag (Strategy pattern, Section 12).
 */
public interface ChatProvider {
    String getReply(String userMessage, String context);
}
