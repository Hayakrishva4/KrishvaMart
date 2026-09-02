package com.krishva.krishvamart.controller;

import com.krishva.krishvamart.dto.LoginRequestDTO;
import com.krishva.krishvamart.dto.RegisterRequestDTO;
import com.krishva.krishvamart.dto.UserResponseDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.filter.AuthFilter;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
    "/api/v1/auth/register",
    "/api/v1/auth/login",
    "/api/v1/auth/logout",
    "/api/v1/auth/me"
})
public class AuthServlet extends BaseApiServlet {
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath() != null ? req.getServletPath() : "";
        String pathInfo = req.getPathInfo() != null ? req.getPathInfo() : "";
        String fullPath = servletPath + pathInfo;

        try {
            if (fullPath.endsWith("/register") || "/api/v1/auth/register".equals(fullPath)) {
                register(req, resp);
            } else if (fullPath.endsWith("/login") || "/api/v1/auth/login".equals(fullPath)) {
                login(req, resp);
            } else if (fullPath.endsWith("/logout") || "/api/v1/auth/logout".equals(fullPath)) {
                logout(req, resp);
            } else {
                JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Unknown route");
            }
        } catch (AppException e) {
            handleError(resp, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath() != null ? req.getServletPath() : "";
        String pathInfo = req.getPathInfo() != null ? req.getPathInfo() : "";
        String fullPath = servletPath + pathInfo;

        if (!fullPath.endsWith("/me") && !"/api/v1/auth/me".equals(fullPath)) {
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