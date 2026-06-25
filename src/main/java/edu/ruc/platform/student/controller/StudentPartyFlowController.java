package edu.ruc.platform.student.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.party.dto.StudentPartyFlowStateResponse;
import edu.ruc.platform.student.service.StudentPartyFlowApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentPartyFlowController {

    private final CurrentUserService currentUserService;
    private final StudentPartyFlowApplicationService studentPartyFlowService;

    @GetMapping("/party-flows")
    public ApiResponse<StudentPartyFlowStateResponse> getPartyFlows() {
        currentUserService.requireAnyRole(RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER, RoleType.ASSISTANT);
        return ApiResponse.success(studentPartyFlowService.getCurrentStudentFlowState());
    }
}
