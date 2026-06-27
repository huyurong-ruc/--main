package edu.ruc.platform.admin.dto;

public record AdminOperationLogFilterRequest(
        String module,
        String action,
        String operatorRole,
        String targetKeyword,
        String result,
        String operatorName,
        String traceId,
        String logLevel,
        java.time.LocalDateTime startTime,
        java.time.LocalDateTime endTime
) {
}
