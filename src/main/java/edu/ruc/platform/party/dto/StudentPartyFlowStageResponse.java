package edu.ruc.platform.party.dto;

public record StudentPartyFlowStageResponse(
        Long id,
        Integer seqNo,
        String name,
        String description,
        Integer expectedDays,
        String status,
        String statusText,
        String statusVariant,
        boolean completed,
        boolean current
) {
}
