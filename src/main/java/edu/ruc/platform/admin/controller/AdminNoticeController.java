package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.admin.dto.AdminNoticeCreateRequest;
import edu.ruc.platform.admin.dto.AdminNoticeFilterRequest;
import edu.ruc.platform.admin.dto.AdminNoticeStatsResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<TargetedNoticeResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.listNotices());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<TargetedNoticeResponse>> page(@RequestParam(required = false) String tab,
                                                                  @RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) String tag,
                                                                  @RequestParam(required = false) String targetKeyword,
                                                                  @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                  @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.pageNotices(new AdminNoticeFilterRequest(tab, keyword, tag, targetKeyword), page, size));
    }

    @GetMapping("/stats")
    public ApiResponse<AdminNoticeStatsResponse> stats(@RequestParam(required = false) String tab,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String tag,
                                                       @RequestParam(required = false) String targetKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.noticeStats(new AdminNoticeFilterRequest(tab, keyword, tag, targetKeyword)));
    }

    @PostMapping
    public ApiResponse<TargetedNoticeResponse> create(@Valid @RequestBody AdminNoticeCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("通知已创建", adminService.createNotice(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TargetedNoticeResponse> update(@PathVariable Long id, @Valid @RequestBody AdminNoticeCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("通知已更新", adminService.updateNotice(id, request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<TargetedNoticeResponse> publish(@PathVariable Long id, @RequestParam boolean published) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (published) {
            return ApiResponse.success("通知已发布", adminService.toggleNoticePublish(id, true));
        }
        return ApiResponse.success("通知已撤回为草稿", adminService.toggleNoticePublish(id, false));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        adminService.deleteNotice(id);
        return ApiResponse.success("通知已删除", null);
    }
}
