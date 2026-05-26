package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogStatsResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/operation-logs")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<AdminOperationLogResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.listOperationLogs());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<AdminOperationLogResponse>> page(@RequestParam(required = false) String module,
                                                                     @RequestParam(required = false) String action,
                                                                     @RequestParam(required = false) String operatorRole,
                                                                     @RequestParam(required = false) String targetKeyword,
                                                                     @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                     @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.pageOperationLogs(
                new AdminOperationLogFilterRequest(
                        module,
                        action,
                        operatorRole,
                        targetKeyword,
                        result,
                        operatorName,
                        traceId,
                        logLevel,
                        parseDateTime(startTime),
                        parseDateTime(endTime)
                ),
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ApiResponse<AdminOperationLogStatsResponse> stats(@RequestParam(required = false) String module,
                                                             @RequestParam(required = false) String result,
                                                             @RequestParam(required = false) String logLevel,
                                                             @RequestParam(required = false) String operatorName,
                                                             @RequestParam(required = false) String traceId,
                                                             @RequestParam(required = false) String action,
                                                             @RequestParam(required = false) String operatorRole,
                                                             @RequestParam(required = false) String targetKeyword,
                                                             @RequestParam(required = false) String startTime,
                                                             @RequestParam(required = false) String endTime) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.operationLogStats(
                new AdminOperationLogFilterRequest(
                        module,
                        action,
                        operatorRole,
                        targetKeyword,
                        result,
                        operatorName,
                        traceId,
                        logLevel,
                        parseDateTime(startTime),
                        parseDateTime(endTime)
                )
        ));
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        if (v.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(v.replace(" ", "T"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
