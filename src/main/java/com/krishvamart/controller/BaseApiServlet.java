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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public abstract class BaseApiServlet extends HttpServlet 
{
 private static final Logger LOG = LoggerFactory.getLogger(BaseApiServlet.class);
 private static final String METHOD_PATCH = "PATCH";
  @Override
   protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
   {
     if (METHOD_PATCH.equalsIgnoreCase(req.getMethod())) 
     {
      try 
      {
       doPatch(req, resp);
      }
      catch (RuntimeException e)
      {
       LOG.error("Unhandled error in PATCH", e);
     handleError(resp, new DataAccessException("Error during patch", e));
         }
            return;
        }
        super.service(req, resp);
    }
 protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
 {
  resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
 }
 protected ServiceRegistry services() 
 {
  return (ServiceRegistry) getServletContext().getAttribute(ServiceRegistry.ATTR);
 }
 protected <T> T readBody(HttpServletRequest req, Class<T> type) throws IOException 
 {
    StringWriter sw = new StringWriter();
        try (BufferedReader reader = req.getReader()) 
        {
            reader.transferTo(sw);
        }
     return JsonUtil.gson().fromJson(sw.toString(), type);
 }
 protected User currentUser(HttpServletRequest req) 
 {
  HttpSession session = req.getSession(false);
    if (session == null) 
     {
      return null;
     }
    Object user = session.getAttribute(AuthFilter.SESSION_USER_ATTR);
        return user instanceof User ? (User) user : null;
 }
 protected User requireUser(HttpServletRequest req) throws UnauthorizedException 
 {
    User user = currentUser(req);
     if (user == null) 
     {
      throw new UnauthorizedException("You must be logged in to perform this action");
     }
        return user;
    }
 protected void handleError(HttpServletResponse resp, Exception e) throws IOException 
 {
  if (e instanceof ValidationException ve) 
  {
    JsonUtil.writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", ve.getMessage());
  }
  else if (e instanceof UnauthorizedException ue) 
  {
    JsonUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", ue.getMessage());
  }
  else if (e instanceof ForbiddenException fe) 
  {
    JsonUtil.writeError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", fe.getMessage());
  } 
  else if (e instanceof NotFoundException nfe) 
  {
    JsonUtil.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", nfe.getMessage());
  }
  else if (e instanceof ConflictException ce) 
  {
    JsonUtil.writeError(resp, HttpServletResponse.SC_CONFLICT, "CONFLICT", ce.getMessage());
  } 
  else if (e instanceof DataAccessException dae) 
  {
    LOG.error("Data access error", dae);
    JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    "INTERNAL_SERVER_ERROR","A server error occurred. Please try again.");
  } 
  else if (e instanceof AppException ae) 
  {
    JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "APP_ERROR", ae.getMessage());
  } 
  else
 {
  LOG.error("Unhandled error", e);
  JsonUtil.writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
     "INTERNAL_SERVER_ERROR","A server error occurred. Please try again.");
 }
}
}