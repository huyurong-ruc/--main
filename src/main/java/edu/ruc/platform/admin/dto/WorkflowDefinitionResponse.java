package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record WorkflowDefinitionResponse(
        Long id,
        String wfCode,
        String wfName,
        String wfType,
        Integer nodeCount,
        boolean active,
        LocalDateTime createdAt
) {
}

