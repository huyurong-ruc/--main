package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.PartyReminderTaskFilterRequest;
import edu.ruc.platform.admin.dto.PartyReminderTaskResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.admin.service.MockAdminService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import edu.ruc.platform.party.domain.LatestPartyReminderTask;
import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyReminderTaskRepository;
import edu.ruc.platform.party.repository.LatestPartyStudentProgressRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/party-reminders")
@RequiredArgsConstructor
public class AdminPartyReminderController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;
    private final LatestPartyReminderTaskRepository latestPartyReminderTaskRepository;
    private final LatestPartyStudentProgressRepository latestPartyStudentProgressRepository;
    private final LatestPartyFlowNodeRepository latestPartyFlowNodeRepository;
    private final StudentProfileRepository studentProfileRepository;

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

    @PutMapping("/{id}")
    public ApiResponse<PartyReminderTaskResponse> updateDueAt(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id,
                                                              @Valid @RequestBody UpdatePartyReminderRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (adminService instanceof MockAdminService mockAdminService) {
            return ApiResponse.success("已更新计划时间", mockAdminService.updatePartyReminderDueAt(id, request.dueAt()));
        }
        if (request == null || request.dueAt() == null) {
            throw new BusinessException("计划时间不能为空");
        }
        LatestPartyReminderTask task = latestPartyReminderTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("提醒任务不存在"));
        task.setDueAt(request.dueAt());
        task = latestPartyReminderTaskRepository.save(task);

        LatestPartyStudentProgress progress = latestPartyStudentProgressRepository.findById(task.getProgressId()).orElse(null);
        Long studentUserId = progress == null ? null : progress.getStudentUserId();
        String studentName = studentUserId == null
                ? null
                : studentProfileRepository.findById(studentUserId).map(p -> p.getName()).orElse(null);
        String studentNo = studentUserId == null
                ? null
                : studentProfileRepository.findById(studentUserId).map(p -> p.getStudentNo()).orElse(null);
        LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(task.getNodeId()).orElse(null);
        return ApiResponse.success("已更新计划时间", new PartyReminderTaskResponse(
                task.getId(),
                task.getProgressId(),
                task.getNodeId(),
                node == null ? null : node.getNodeName(),
                studentUserId,
                studentName,
                studentNo,
                task.getDueAt(),
                task.getChannel(),
                task.getStatus(),
                task.getSentAt(),
                null,
                task.getCreatedAt()
        ));
    }

    public record UpdatePartyReminderRequest(LocalDateTime dueAt) {
    }
}
