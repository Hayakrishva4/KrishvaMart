package com.krishva.krishvamart.controller;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.exception.NotFoundException;
import com.krishva.krishvamart.exception.UnauthorizedException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.filter.AuthFilter;
import com.krishva.krishvamart.listener.ServiceRegistry;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public abstract class BaseApiServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(BaseApiServlet.class);
    private static final String METHOD_PATCH = "PATCH";
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws javax.servlet.ServletException, IOException {
        if (METHOD_PATCH.equalsIgnoreCase(req.getMethod())) {
            try {
                doPatch(req, resp);
            } catch (RuntimeException e) {
                LOG.error("Unhandled error in PATCH", e);
                handleError(resp, new com.krishva.krishvamart.exception.DataAccessException("Unhandled error", e));
            }
            return;
        }
        super.service(req, resp);
    }
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    protected ServiceRegistry services() {
        return (ServiceRegistry) getServletContext().getAttribute(ServiceRegistry.ATTR);
    }
    protected <T> T readBody(HttpServletRequest req, Class<T> type) throws IOException {
        StringWriter sw = new StringWriter();
        try (BufferedReader reader = req.getReader()) {
            reader.transferTo(sw);
        }
        return JsonUtil.gson().fromJson(sw.toString(), type);
    }

    protected User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(AuthFilter.SESSION_USER_ATTR);
        return user instanceof User ? (User) user : null;
    }

    protected User requireUser(HttpServletRequest req) throws UnauthorizedException {
        User user = currentUser(req);
        if (user == null) {
            throw new UnauthorizedException("You must be logged in to perform this action");
        }
        return user;
    }

    protected void handleError(HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof ValidationException ve) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, ve.getErrorCode(), ve.getMessage());
        } else if (e instanceof UnauthorizedException ue) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, ue.getErrorCode(), ue.getMessage());
        } else if (e instanceof ForbiddenException fe) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, fe.getErrorCode(), fe.getMessage());
        } else if (e instanceof NotFoundException nfe) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, nfe.getErrorCode(), nfe.getMessage());
        } else if (e instanceof ConflictException ce) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, ce.getErrorCode(), ce.getMessage());
        } else if (e instanceof DataAccessException dae) {
            LOG.error("Data access error", dae);
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, dae.getErrorCode(),
                    "A server error occurred. Please try again.");
        } else if (e instanceof AppException ae) {
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ae.getErrorCode(), ae.getMessage());
        } else {
            // Never leak stack traces to the client (Section 9 checklist).
            LOG.error("Unhandled error", e);
            JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "A server error occurred. Please try again.");
        }
    }
}
