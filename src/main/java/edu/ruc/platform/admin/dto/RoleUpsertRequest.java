package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleUpsertRequest(
        @NotBlank(message = "角色编码不能为空")
        String roleCode,
        @NotBlank(message = "角色名称不能为空")
        String roleName,
        String permissions,
        Boolean isActive
) {
}
