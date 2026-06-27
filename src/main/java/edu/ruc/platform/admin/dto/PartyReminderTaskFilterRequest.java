package edu.ruc.platform.admin.dto;

public record PartyReminderTaskFilterRequest(
        String status,
        String channel,
        String studentKeyword
) {
}
