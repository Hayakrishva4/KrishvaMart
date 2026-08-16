package com.krishva.krishvamart.controller;
import com.krishva.krishvamart.listener.AppContextListener;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
public class HealthServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> health = new LinkedHashMap<>();
        health.put("status", "UP");
        DataSource dataSource = (DataSource) getServletContext().getAttribute(AppContextListener.DATASOURCE_ATTR);
        try (Connection conn = dataSource.getConnection()) {
            health.put("db", conn.isValid(2) ? "UP" : "DOWN");
        } catch (Exception e) {
            health.put("db", "DOWN");
        }
        boolean allUp = health.values().stream().allMatch("UP"::equals);
        JsonUtil.writeSuccess(resp, allUp ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR, health);
    }
}