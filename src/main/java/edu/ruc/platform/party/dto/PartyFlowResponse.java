package edu.ruc.platform.party.dto;

import java.time.LocalDateTime;

public record PartyFlowResponse(
        Long id,
        String flowCode,
        String flowName,
        String flowType,
        Integer nodeCount,
        boolean active,
        LocalDateTime createdAt
) {
}

