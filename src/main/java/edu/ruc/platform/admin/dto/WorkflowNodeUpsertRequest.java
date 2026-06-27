package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WorkflowNodeUpsertRequest(
        @NotBlank(message = "节点名称不能为空") String nodeName,
        String approverRole,
        @Min(value = 0, message = "SLA时长不能小于0") Integer slaHours,
        Boolean allowReject,
        @Min(value = 1, message = "顺序号不能小于1") Integer seqNo
) {
}

