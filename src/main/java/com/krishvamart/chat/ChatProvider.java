package com.krishva.krishvamart.chat;
public interface ChatProvider {
    String getReply(String userMessage, String context);
}
