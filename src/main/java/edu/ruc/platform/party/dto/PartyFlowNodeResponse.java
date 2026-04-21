package edu.ruc.platform.party.dto;

public record PartyFlowNodeResponse(
        Long id,
        Long flowId,
        Integer seqNo,
        String nodeName,
        Integer expectedDays,
        Integer reminderEveryDays,
        Integer overdueDays
) {
}

