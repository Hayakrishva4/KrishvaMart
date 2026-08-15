package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.dto.SellerSalesSummaryDTO;
import com.krishva.krishvamart.exception.DataAccessException;

/** Read-only aggregate queries for the O3 seller sales dashboard. All SQL, PreparedStatement only. */
public interface AnalyticsDAO {

    /** O3: order counts, units sold, and revenue for a seller, counting only DELIVERED orders as completed sales. */
    SellerSalesSummaryDTO sellerSummary(long sellerId) throws DataAccessException;
}
