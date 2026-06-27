package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkflowDefinitionUpsertRequest(
        @NotBlank(message = "流程编码不能为空") String wfCode,
        @NotBlank(message = "流程名称不能为空") String wfName,
        @NotBlank(message = "类型不能为空") String wfType,
        @NotBlank(message = "业务类型不能为空") String businessType,
        Boolean active
) {
}

