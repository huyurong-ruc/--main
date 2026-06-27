package edu.ruc.platform.admin.dto;

public record QaTicketMessageResponse(
        Long id,
        String actorName,
        String actorRole,
        String createdAt,
        String content
) {
}
