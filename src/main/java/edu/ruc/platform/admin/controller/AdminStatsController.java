package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.AdminStatsResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminApplicationService adminService;
    private final CertificateApplicationService certificateService;

    @GetMapping
    public ApiResponse<AdminStatsResponse> stats() {
        int pending = (int) certificateService.listApprovalTasks().stream()
                .filter(item -> {
                    String status = item.status();
                    if (status == null) {
                        return false;
                    }
                    String upper = status.toUpperCase();
                    return "PENDING".equals(upper) || "COUNSELOR_APPROVED".equals(upper);
                })
                .count();
        return ApiResponse.success(adminService.stats(pending));
    }
}
