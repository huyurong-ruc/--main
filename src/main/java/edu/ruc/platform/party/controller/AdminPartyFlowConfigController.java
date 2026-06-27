package edu.ruc.platform.party.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.party.dto.PartyFlowNodeResponse;
import edu.ruc.platform.party.dto.PartyFlowNodeUpsertRequest;
import edu.ruc.platform.party.dto.PartyFlowResponse;
import edu.ruc.platform.party.dto.PartyFlowUpsertRequest;
import edu.ruc.platform.party.service.PartyFlowConfigApplicationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/party-flows")
@RequiredArgsConstructor
public class AdminPartyFlowConfigController {

    private final PartyFlowConfigApplicationService service;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<PartyFlowResponse>> listFlows() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.listFlows());
    }

    @PostMapping
    public ApiResponse<PartyFlowResponse> createFlow(@Validated @RequestBody PartyFlowUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.createFlow(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PartyFlowResponse> updateFlow(@Positive(message = "流程ID必须大于0") @PathVariable Long id,
                                                     @Validated @RequestBody PartyFlowUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.updateFlow(id, request));
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<PartyFlowResponse> copyFlow(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.copyFlow(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteFlow(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        service.deleteFlow(id);
        return ApiResponse.success(true);
    }

    @GetMapping("/{id}/nodes")
    public ApiResponse<List<PartyFlowNodeResponse>> listFlowNodes(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.listFlowNodes(id));
    }

    @PostMapping("/{id}/nodes")
    public ApiResponse<PartyFlowNodeResponse> createFlowNode(@Positive(message = "流程ID必须大于0") @PathVariable Long id,
                                                             @Validated @RequestBody PartyFlowNodeUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.createFlowNode(id, request));
    }

    @PutMapping("/nodes/{nodeId}")
    public ApiResponse<PartyFlowNodeResponse> updateFlowNode(@Positive(message = "节点ID必须大于0") @PathVariable Long nodeId,
                                                             @Validated @RequestBody PartyFlowNodeUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.updateFlowNode(nodeId, request));
    }

    @PostMapping("/nodes/{nodeId}/move")
    public ApiResponse<List<PartyFlowNodeResponse>> moveFlowNode(@Positive(message = "节点ID必须大于0") @PathVariable Long nodeId,
                                                                 @NotBlank(message = "direction不能为空") @RequestParam String direction) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(service.moveFlowNode(nodeId, direction));
    }

    @DeleteMapping("/nodes/{nodeId}")
    public ApiResponse<Boolean> deleteFlowNode(@Positive(message = "节点ID必须大于0") @PathVariable Long nodeId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        service.deleteFlowNode(nodeId);
        return ApiResponse.success(true);
    }
}

