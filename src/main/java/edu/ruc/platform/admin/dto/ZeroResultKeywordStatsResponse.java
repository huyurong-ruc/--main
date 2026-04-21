package edu.ruc.platform.admin.dto;

public record ZeroResultKeywordStatsResponse(
        String keyword,
        Integer missCount,
        String lastDate
) {
}

