package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.AnalyticsDAO;
import com.krishva.krishvamart.dto.SellerSalesSummaryDTO;
import com.krishva.krishvamart.exception.AppException;

public class SellerAnalyticsService {

    private final AnalyticsDAO analyticsDAO;

    public SellerAnalyticsService(AnalyticsDAO analyticsDAO) {
        this.analyticsDAO = analyticsDAO;
    }

    public SellerSalesSummaryDTO summaryFor(long sellerId)
            throws AppException {
        return analyticsDAO.sellerSummary(sellerId);
    }
}