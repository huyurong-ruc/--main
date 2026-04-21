package edu.ruc.platform.admin.dto;

import java.util.List;

public record QaTicketDetailResponse(
        Long id,
        String studentName,
        Long studentId,
        String createdAt,
        String status,
        String questionText,
        List<QaTicketMessageResponse> messages
) {
}

