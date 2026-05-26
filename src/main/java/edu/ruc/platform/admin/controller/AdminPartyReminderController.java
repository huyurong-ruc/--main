package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.PartyReminderTaskFilterRequest;
import edu.ruc.platform.admin.dto.PartyReminderTaskResponse;
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
@RequestMapping("/api/v1/admin/party-reminders")
@RequiredArgsConstructor
public class AdminPartyReminderController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<PartyReminderTaskResponse>> list(@RequestParam(required = false) String status,
                                                             @RequestParam(required = false) String channel,
                                                             @RequestParam(required = false) String studentKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listPartyReminderTasks(new PartyReminderTaskFilterRequest(status, channel, studentKeyword)));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<PartyReminderTaskResponse>> page(@RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String channel,
                                                                     @RequestParam(required = false) String studentKeyword,
                                                                     @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                     @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pagePartyReminderTasks(new PartyReminderTaskFilterRequest(status, channel, studentKeyword), page, size));
    }

    @PostMapping("/{id}/send")
    public ApiResponse<PartyReminderTaskResponse> send(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("提醒已发送", adminService.sendPartyReminder(id));
    }

    @PostMapping("/{id}/resend")
    public ApiResponse<PartyReminderTaskResponse> resend(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("提醒已重新发送", adminService.resendPartyReminder(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PartyReminderTaskResponse> cancel(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("提醒已取消", adminService.cancelPartyReminder(id));
    }
}
