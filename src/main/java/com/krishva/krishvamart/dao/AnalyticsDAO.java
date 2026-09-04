package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.dto.SellerSalesSummaryDTO;
import com.krishva.krishvamart.exception.DataAccessException;

public interface AnalyticsDAO {
 SellerSalesSummaryDTO sellerSummary(long sellerId) throws DataAccessException;
}
