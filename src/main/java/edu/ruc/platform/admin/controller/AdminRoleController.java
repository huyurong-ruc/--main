package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.RoleResponse;
import edu.ruc.platform.admin.dto.RoleUpsertRequest;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<RoleResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.listRoles());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> detail(@Positive(message = "角色ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.getRole(id));
    }

    @PostMapping
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN);
        return ApiResponse.success("角色创建成功", adminService.createRole(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> update(@Positive(message = "角色ID必须大于0") @PathVariable Long id,
                                            @Valid @RequestBody RoleUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN);
        return ApiResponse.success("角色更新成功", adminService.updateRole(id, request));
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<RoleResponse> copy(@Positive(message = "角色ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN);
        return ApiResponse.success("角色复制成功", adminService.copyRole(id));
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<RoleResponse> toggle(@Positive(message = "角色ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN);
        return ApiResponse.success("角色状态已切换", adminService.toggleRole(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "角色ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN);
        adminService.deleteRole(id);
        return ApiResponse.success("角色删除成功", null);
    }
}
