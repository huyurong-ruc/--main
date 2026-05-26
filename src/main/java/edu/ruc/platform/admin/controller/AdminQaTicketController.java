package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.QaTicketDetailResponse;
import edu.ruc.platform.admin.dto.QaTicketListItemResponse;
import edu.ruc.platform.admin.dto.QaTicketReplyRequest;
import edu.ruc.platform.admin.service.QaTicketApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@Validated
@RequestMapping("/api/v1/admin/qa-tickets")
@RequiredArgsConstructor
public class AdminQaTicketController {

    private final QaTicketApplicationService service;
    private final CurrentUserService currentUserService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<QaTicketListItemResponse>> page(@RequestParam(required = false) String status,
                                                                    @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                    @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.pageTickets(status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<QaTicketDetailResponse> detail(@Positive(message = "工单ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.getDetail(id));
    }

    @PostMapping("/{id}/take")
    public ApiResponse<QaTicketDetailResponse> take(@Positive(message = "工单ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.take(id));
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<QaTicketDetailResponse> reply(@Positive(message = "工单ID必须大于0") @PathVariable Long id,
                                                     @Valid @RequestBody QaTicketReplyRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.reply(id, request));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<QaTicketDetailResponse> close(@Positive(message = "工单ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.close(id));
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<QaTicketDetailResponse> deleteMessage(@Positive(message = "消息ID必须大于0") @PathVariable Long messageId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.deleteMessage(messageId));
    }

    @PostMapping("/messages/{messageId}/withdraw")
    public ApiResponse<QaTicketDetailResponse> withdrawMessage(@Positive(message = "消息ID必须大于0") @PathVariable Long messageId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.withdrawMessage(messageId));
    }
}
