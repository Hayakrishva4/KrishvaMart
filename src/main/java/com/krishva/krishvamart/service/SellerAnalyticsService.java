package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.AnalyticsDAO;
import com.krishva.krishvamart.dto.SellerSalesSummaryDTO;
import com.krishva.krishvamart.exception.AppException;

/** Business rules for O3 (seller sales dashboard). No JDBC here - all aggregation goes through AnalyticsDAO. */
public class SellerAnalyticsService {

    private final AnalyticsDAO analyticsDAO;

    public SellerAnalyticsService(AnalyticsDAO analyticsDAO) {
        this.analyticsDAO = analyticsDAO;
    }

    /** O3: order counts, units sold, and revenue for a seller, plus a per-product breakdown. */
    public SellerSalesSummaryDTO summaryFor(long sellerId) throws AppException {
        return analyticsDAO.sellerSummary(sellerId);
    }
}
