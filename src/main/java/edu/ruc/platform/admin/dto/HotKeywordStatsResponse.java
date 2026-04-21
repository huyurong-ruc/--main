package edu.ruc.platform.admin.dto;

public record HotKeywordStatsResponse(
        String keyword,
        Integer count,
        Integer deltaPct
) {
}

