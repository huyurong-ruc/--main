package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record RoleResponse(
        Long id,
        String roleCode,
        String roleName,
        String permissions,
        Boolean isActive,
        Integer userCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
