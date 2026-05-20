package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record WorkflowDefinitionResponse(
        Long id,
        String wfCode,
        String wfName,
        String wfType,
        String businessType,
        Integer nodeCount,
        boolean active,
        LocalDateTime createdAt
) {
}
