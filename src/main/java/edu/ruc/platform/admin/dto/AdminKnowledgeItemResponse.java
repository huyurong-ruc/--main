package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record AdminKnowledgeItemResponse(
        Long id,
        String title,
        String category,
        String tags,
        Integer version,
        boolean published,
        String officialUrl,
        String sourceFileName,
        String audienceScope,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
