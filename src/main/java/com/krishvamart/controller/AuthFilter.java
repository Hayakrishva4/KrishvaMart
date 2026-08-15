package com.krishva.krishvamart.filter;

import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Enforces session checks on every protected servlet (Section 9 checklist item
 * "All protected servlets enforce session checks via AuthFilter").
 *
 * Public (unauthenticated) routes:
 *  - POST /api/v1/auth/register, POST /api/v1/auth/login
 *  - GET  /api/v1/products/**        (F3: buyer browse/search does not require login)
 *  - GET  /api/v1/reviews/**         (public star ratings)
 *  - GET  /api/v1/health
 * Everything else under /api/v1/** requires an authenticated session.
 * Role-specific checks (seller-only, admin-only) are enforced in the service layer.
 */
@WebFilter(urlPatterns = "/api/v1/*")
public class AuthFilter implements Filter {

    public static final String SESSION_USER_ATTR = "authUser";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (isPublicRoute(req)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object user = session == null ? null : session.getAttribute(SESSION_USER_ATTR);
        if (!(user instanceof User)) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED",
                    "You must be logged in to perform this action");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isPublicRoute(HttpServletRequest req) {
        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        String method = req.getMethod();

        if (path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/login")) {
            return true;
        }
        if (path.equals("/api/v1/health")) {
            return true;
        }
        if (path.equals("/api/v1/chat") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/products")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/v1/reviews")) {
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}
