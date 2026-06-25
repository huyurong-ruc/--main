package edu.ruc.platform.party.dto;

import java.util.List;

public record StudentPartyFlowStateResponse(
        boolean hasAnyFlow,
        String emptyTitle,
        String emptyDescription,
        String teacherAuditTip,
        List<StudentPartyFlowItemResponse> flows
) {
}
