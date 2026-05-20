package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminCertTemplateUpsertRequest(
        @NotBlank(message = "模板编码不能为空") String templateCode,
        @NotBlank(message = "模板名称不能为空") String templateName,
        @NotBlank(message = "输出格式不能为空") String outputFormat,
        String keywords,
        Long fileId,
        Boolean active
) {
}

