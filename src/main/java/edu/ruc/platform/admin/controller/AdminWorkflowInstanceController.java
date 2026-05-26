package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.WorkflowInstanceFilterRequest;
import edu.ruc.platform.admin.dto.WorkflowInstanceResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/workflow-instances")
@RequiredArgsConstructor
public class AdminWorkflowInstanceController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<WorkflowInstanceResponse>> list(@RequestParam(required = false) String status,
                                                            @RequestParam(required = false) String businessType,
                                                            @RequestParam(required = false) String startedByKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.listWorkflowInstances(new WorkflowInstanceFilterRequest(status, businessType, startedByKeyword)));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<WorkflowInstanceResponse>> page(@RequestParam(required = false) String status,
                                                                    @RequestParam(required = false) String businessType,
                                                                    @RequestParam(required = false) String startedByKeyword,
                                                                    @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                    @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.pageWorkflowInstances(new WorkflowInstanceFilterRequest(status, businessType, startedByKeyword), page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowInstanceResponse> detail(@Positive(message = "流程实例ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.getWorkflowInstance(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<WorkflowInstanceResponse> cancel(@Positive(message = "流程实例ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success("流程实例已取消", adminService.cancelWorkflowInstance(id));
    }
}
