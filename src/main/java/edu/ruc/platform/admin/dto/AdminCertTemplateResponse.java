package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record AdminCertTemplateResponse(
        Long id,
        String templateCode,
        String templateName,
        String outputFormat,
        boolean active,
        String keywords,
        Long fileId,
        String fileName,
        LocalDateTime createdAt
) {
}

