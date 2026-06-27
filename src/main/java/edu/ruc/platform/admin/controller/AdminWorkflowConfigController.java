package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.WorkflowDefinitionResponse;
import edu.ruc.platform.admin.dto.WorkflowDefinitionUpsertRequest;
import edu.ruc.platform.admin.dto.WorkflowNodeResponse;
import edu.ruc.platform.admin.dto.WorkflowNodeUpsertRequest;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/workflows")
@RequiredArgsConstructor
public class AdminWorkflowConfigController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<WorkflowDefinitionResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.listWorkflowDefinitions());
    }

    @PostMapping
    public ApiResponse<WorkflowDefinitionResponse> create(@Validated @RequestBody WorkflowDefinitionUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.createWorkflowDefinition(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkflowDefinitionResponse> update(@Positive(message = "流程ID必须大于0") @PathVariable Long id,
                                                          @Validated @RequestBody WorkflowDefinitionUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.updateWorkflowDefinition(id, request));
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<WorkflowDefinitionResponse> copy(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.copyWorkflowDefinition(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        adminService.deleteWorkflowDefinition(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/{id}/nodes")
    public ApiResponse<List<WorkflowNodeResponse>> listNodes(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.listWorkflowNodes(id));
    }

    @PostMapping("/{id}/nodes")
    public ApiResponse<WorkflowNodeResponse> createNode(@Positive(message = "流程ID必须大于0") @PathVariable Long id,
                                                        @Validated @RequestBody WorkflowNodeUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.createWorkflowNode(id, request));
    }

    @PutMapping("/nodes/{nodeId}")
    public ApiResponse<WorkflowNodeResponse> updateNode(@Positive(message = "节点ID必须大于0") @PathVariable Long nodeId,
                                                        @Validated @RequestBody WorkflowNodeUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.updateWorkflowNode(nodeId, request));
    }

    @PostMapping("/nodes/{nodeId}/move")
    public ApiResponse<List<WorkflowNodeResponse>> moveNode(@Positive(message = "节点ID必须大于0") @PathVariable Long nodeId,
                                                            @NotBlank(message = "direction不能为空") @RequestParam String direction) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.moveWorkflowNode(nodeId, direction));
    }

    @DeleteMapping("/nodes/{nodeId}")
    public ApiResponse<Boolean> deleteNode(@Positive(message = "节点ID必须大于0") @PathVariable Long nodeId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        adminService.deleteWorkflowNode(nodeId);
        return ApiResponse.success(true);
    }
}

