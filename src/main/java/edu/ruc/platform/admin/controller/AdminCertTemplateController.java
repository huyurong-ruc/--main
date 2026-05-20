package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.AdminCertTemplateResponse;
import edu.ruc.platform.admin.dto.AdminCertTemplateUpsertRequest;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
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
@RequestMapping("/api/v1/admin/cert-templates")
@RequiredArgsConstructor
public class AdminCertTemplateController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<AdminCertTemplateResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listCertTemplates());
    }

    @PostMapping
    public ApiResponse<AdminCertTemplateResponse> create(@Validated @RequestBody AdminCertTemplateUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.createCertTemplate(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminCertTemplateResponse> update(@Positive(message = "模板ID必须大于0") @PathVariable Long id,
                                                         @Validated @RequestBody AdminCertTemplateUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.updateCertTemplate(id, request));
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<AdminCertTemplateResponse> copy(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.copyCertTemplate(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        adminService.deleteCertTemplate(id);
        return ApiResponse.success(true);
    }
}
