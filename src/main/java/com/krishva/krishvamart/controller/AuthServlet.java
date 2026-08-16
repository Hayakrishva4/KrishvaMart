package com.krishva.krishvamart.controller;
import com.krishva.krishvamart.dto.LoginRequestDTO;
import com.krishva.krishvamart.dto.RegisterRequestDTO;
import com.krishva.krishvamart.dto.UserResponseDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.filter.AuthFilter;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
public class AuthServlet extends BaseApiServlet {
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            switch (req.getServletPath()) {
                case "/api/v1/auth/register" -> register(req, resp);
                case "/api/v1/auth/login" -> login(req, resp);
                case "/api/v1/auth/logout" -> logout(req, resp);
                default -> JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown route");
            }
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"/api/v1/auth/me".equals(req.getServletPath())) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown route");
            return;
        }
        User user = currentUser(req);
        if (user == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Not logged in");
            return;
        }
        JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, UserResponseDTO.from(user));
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException, AppException {
        RegisterRequestDTO body = readBody(req, RegisterRequestDTO.class);
        if (body == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Request body required");
            return;
        }
        User user = services().userService().register(body.getName(), body.getEmail(), body.getPassword(), body.getRole());
        establishSession(req, user);
        JsonUtil.writeSuccess(resp, HttpServletResponse.SC_CREATED, UserResponseDTO.from(user));
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException, AppException {
        LoginRequestDTO body = readBody(req, LoginRequestDTO.class);
        if (body == null) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Request body required");
            return;
        }
        User user = services().userService().login(body.getEmail(), body.getPassword());
        establishSession(req, user);
        JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, UserResponseDTO.from(user));
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, null);
    }

    private void establishSession(HttpServletRequest req, User user) {
        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute(AuthFilter.SESSION_USER_ATTR, user);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
    }
}