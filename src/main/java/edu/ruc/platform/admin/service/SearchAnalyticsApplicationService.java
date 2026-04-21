package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.SearchAnalyticsSummaryResponse;

public interface SearchAnalyticsApplicationService {

    SearchAnalyticsSummaryResponse getSummary(int days);
}

