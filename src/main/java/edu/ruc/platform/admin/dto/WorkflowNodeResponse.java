package edu.ruc.platform.admin.dto;

public record WorkflowNodeResponse(
        Long id,
        Long wfId,
        Integer seqNo,
        String nodeName,
        String approverRole,
        Integer slaHours,
        boolean allowReject
) {
}

