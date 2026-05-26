package edu.ruc.platform.admin.dto;

public record WorkflowInstanceFilterRequest(
        String status,
        String businessType,
        String startedByKeyword
) {
}
