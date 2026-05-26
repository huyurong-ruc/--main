package edu.ruc.platform.admin.dto;

public record AdminNoticeFilterRequest(
        String tab,
        String keyword,
        String tag,
        String targetKeyword
) {
}
