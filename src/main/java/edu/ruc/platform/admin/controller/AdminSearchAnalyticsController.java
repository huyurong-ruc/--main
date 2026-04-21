package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.SearchAnalyticsSummaryResponse;
import edu.ruc.platform.admin.service.SearchAnalyticsApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/admin/search-analytics")
@RequiredArgsConstructor
public class AdminSearchAnalyticsController {

    private final SearchAnalyticsApplicationService service;
    private final CurrentUserService currentUserService;

    @GetMapping("/summary")
    public ApiResponse<SearchAnalyticsSummaryResponse> summary(@Min(value = 1, message = "days不能小于1")
                                                               @Max(value = 90, message = "days不能大于90")
                                                               @RequestParam(defaultValue = "7") int days) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.getSummary(days));
    }
}

