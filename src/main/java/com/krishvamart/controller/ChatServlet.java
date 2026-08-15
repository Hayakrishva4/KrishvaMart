package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.util.JsonUtil;

import java.io.IOException;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * F/O4 (mandatory deliverable for Final Review): POST /api/v1/chat.
 * Validates input then delegates to ChatService, which calls the configured
 * ChatProvider server-side and enforces the Section 17 guardrails.
 */
@WebServlet(urlPatterns = "/api/v1/chat")
public class ChatServlet extends BaseApiServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<?, ?> body = readBody(req, Map.class);
        Object messageObj = body == null ? null : body.get("message");
        String message = messageObj == null ? null : messageObj.toString();
        if (message == null || message.isBlank()) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "message is required");
            return;
        }
        String sessionId = req.getSession(true).getId();
        String reply = services().chatService().reply(sessionId, message);
        JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, Map.of("reply", reply));
    }
}
