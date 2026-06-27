package edu.ruc.platform.party.dto;

import java.time.LocalDate;
import java.util.List;

public record StudentPartyFlowItemResponse(
        Long flowId,
        String flowCode,
        String flowName,
        String flowType,
        boolean active,
        boolean hasProgress,
        String currentStage,
        Integer progressPercent,
        LocalDate stageStartDate,
        LocalDate nextDeadline,
        String completedActions,
        String nextAction,
        String nextActionRule,
        List<StudentPartyFlowStageResponse> stages
) {
}
