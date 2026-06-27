package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskFilterRequest;
import edu.ruc.platform.certificate.dto.ApprovalTaskStatsResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.certificate.service.MockCertificateService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final CertificateApplicationService certificateService;
    private final CurrentUserService currentUserService;

    record AssignApprovalTaskRequest(Long targetUserId, String targetName) {
    }

    @GetMapping
    public ApiResponse<List<ApprovalTaskResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.listApprovalTasks());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<ApprovalTaskResponse>> page(@Positive(message = "学生ID必须大于 0") @RequestParam(required = false) Long studentId,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) String certificateType,
                                                                @RequestParam(required = false) String keyword,
                                                                @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.pageApprovalTasks(
                new ApprovalTaskFilterRequest(studentId, status, certificateType, keyword),
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ApiResponse<ApprovalTaskStatsResponse> stats(@Positive(message = "学生ID必须大于 0") @RequestParam(required = false) Long studentId,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String certificateType,
                                                        @RequestParam(required = false) String keyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.approvalTaskStats(
                new ApprovalTaskFilterRequest(studentId, status, certificateType, keyword)
        ));
    }

    @GetMapping("/{requestId}/history")
    public ApiResponse<List<ApprovalHistoryResponse>> history(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.listApprovalHistory(requestId));
    }

    @PostMapping("/{requestId}/action")
    public ApiResponse<ApprovalTaskResponse> action(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId,
                                                    @Valid @RequestBody ApprovalActionRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("审批已处理", certificateService.handleApproval(requestId, request));
    }

    @PostMapping("/{requestId}/assign")
    public ApiResponse<ApprovalTaskResponse> assign(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId,
                                                    @RequestBody AssignApprovalTaskRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        if (!(certificateService instanceof MockCertificateService mock)) {
            throw new BusinessException("转派接口未接入");
        }
        if (request == null || request.targetUserId() == null) {
            throw new BusinessException("请选择目标老师");
        }
        return ApiResponse.success("已转派审批任务", mock.assignApprovalTask(requestId, request.targetUserId(), request.targetName()));
    }

    @PostMapping("/seed")
    public ApiResponse<List<ApprovalTaskResponse>> seed(@RequestParam(defaultValue = "2") @Min(value = 1, message = "count 不能小于 1") int count) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (!(certificateService instanceof MockCertificateService mock)) {
            throw new BusinessException("仅 mock 环境支持生成模拟审批任务");
        }
        mock.seedApprovalTasks(count);
        return ApiResponse.success("已生成模拟审批任务", certificateService.listApprovalTasks());
    }
}
