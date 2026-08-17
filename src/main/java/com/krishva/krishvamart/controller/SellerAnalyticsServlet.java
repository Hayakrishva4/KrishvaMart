package com.krishva.krishvamart.controller;
import com.krishva.krishvamart.dto.SellerSalesSummaryDTO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ForbiddenException;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.JsonUtil;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@WebServlet(urlPatterns = "/api/v1/seller/analytics")
public class SellerAnalyticsServlet extends BaseApiServlet 
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try 
        {
            User user = requireUser(req);
            if (user.getRole() != User.Role.SELLER) 
            {
             throw new ForbiddenException("Only Sellers have a Sales Dashboard!");
            }
            SellerSalesSummaryDTO summary = services().sellerAnalyticsService().summaryFor(user.getId());
            JsonUtil.writeSuccess(resp, HttpServletResponse.SC_OK, summary);
        } catch (AppException e) {
            handleError(resp, e);
        }
    }
}
