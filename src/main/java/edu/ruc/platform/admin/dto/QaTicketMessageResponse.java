package edu.ruc.platform.admin.dto;

public record QaTicketMessageResponse(
        String actorName,
        String actorRole,
        String createdAt,
        String content
) {
}

