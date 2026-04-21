package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;

public record PartyFlowUpsertRequest(
        @NotBlank(message = "流程编码不能为空") String flowCode,
        @NotBlank(message = "流程名称不能为空") String flowName,
        @NotBlank(message = "类型不能为空") String flowType,
        Boolean active
) {
}

