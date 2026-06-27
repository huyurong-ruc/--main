package edu.ruc.platform.admin.dto;

public record QaTicketListItemResponse(
        Long id,
        String studentName,
        String status,
        String summary,
        String createdAt
) {
}

