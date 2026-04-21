package edu.ruc.platform.admin.dto;

import java.util.List;

public record SearchAnalyticsSummaryResponse(
        List<SearchTrendPointResponse> trend,
        List<HotKeywordStatsResponse> hotKeywords,
        List<ZeroResultKeywordStatsResponse> zeroResults
) {
}

