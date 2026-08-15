package com.krishvamart.chat;
public interface ChatProvider {
    String getReply(String userMessage, String context);
}
