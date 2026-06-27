package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PartyFlowNodeUpsertRequest(
        @NotBlank(message = "节点名称不能为空") String nodeName,
        @Min(value = 1, message = "顺序号不能小于1") Integer seqNo,
        @Min(value = 0, message = "提醒阈值不能小于0") Integer reminderEveryDays,
        @Min(value = 0, message = "提交时间不能小于0") Integer expectedDays,
        @Min(value = 0, message = "逾期阈值不能小于0") Integer overdueDays
) {
}

