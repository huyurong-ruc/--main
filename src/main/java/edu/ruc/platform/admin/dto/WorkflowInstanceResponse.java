package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record WorkflowInstanceResponse(
        Long id,
        String wfCode,
        String wfName,
        String businessType,
        String businessTable,
        Long businessId,
        String status,
        Long startedBy,
        String startedByName,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
