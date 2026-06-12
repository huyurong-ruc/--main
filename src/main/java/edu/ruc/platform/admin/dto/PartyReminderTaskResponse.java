package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record PartyReminderTaskResponse(
        Long id,
        Long progressId,
        Long flowId,
        String flowName,
        Long nodeId,
        String nodeName,
        Long studentUserId,
        String studentName,
        String studentNo,
        LocalDateTime dueAt,
        String channel,
        String status,
        LocalDateTime sentAt,
        String errorMessage,
        LocalDateTime createdAt
) {
}
