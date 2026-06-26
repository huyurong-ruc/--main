package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.LatestPartyFlow;
import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import edu.ruc.platform.party.dto.StudentPartyFlowItemResponse;
import edu.ruc.platform.party.dto.StudentPartyFlowStageResponse;
import edu.ruc.platform.party.dto.StudentPartyFlowStateResponse;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyFlowRepository;
import edu.ruc.platform.party.repository.LatestPartyStudentProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseStudentPartyFlowService implements StudentPartyFlowApplicationService {

    private static final String EMPTY_TITLE = "您暂未开启任何党团流程";
    private static final String EMPTY_DESCRIPTION = "当前账号在 Kingbase 中尚未关联到任何党团流程记录";
    private static final String READONLY_TIP = "学生端仅支持查看，流程状态由管理端教师审核更新。";

    private final CurrentUserService currentUserService;
    private final LatestPartyFlowRepository flowRepository;
    private final LatestPartyFlowNodeRepository nodeRepository;
    private final LatestPartyStudentProgressRepository progressRepository;

    @Override
    public StudentPartyFlowStateResponse getCurrentStudentFlowState() {
        Long studentId = currentStudentId();
        List<StudentPartyFlowItemResponse> flows = flowRepository.findByIsDeletedOrderByIdAsc(0).stream()
                .filter(flow -> flow.getIsActive() != null && flow.getIsActive() == 1)
                .sorted(Comparator.comparing(LatestPartyFlow::getId))
                .map(flow -> buildFlowItem(studentId, flow))
                .toList();
        return new StudentPartyFlowStateResponse(
                !flows.isEmpty(),
                EMPTY_TITLE,
                EMPTY_DESCRIPTION,
                READONLY_TIP,
                flows
        );
    }

    private StudentPartyFlowItemResponse buildFlowItem(Long studentId, LatestPartyFlow flow) {
        List<LatestPartyFlowNode> nodes = nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flow.getId(), 0);
        LatestPartyStudentProgress progress = progressRepository.findByStudentUserIdAndFlowIdAndIsDeleted(studentId, flow.getId(), 0)
                .orElse(null);

        int currentIndex = resolveCurrentIndex(progress, nodes);
        boolean hasProgress = progress != null;
        boolean completedNode = progress != null && isCompleted(progress);
        boolean completedFlow = isCompletedFlow(progress, nodes, currentIndex);
        List<StudentPartyFlowStageResponse> stages = buildStages(nodes, currentIndex, completedNode, completedFlow);
        int progressPercent = calcProgressPercent(stages, hasProgress, completedFlow);
        LocalDate stageStartDate = resolveStageStartDate(progress);
        LocalDate nextDeadline = resolveNextDeadline(progress, nodes, currentIndex, stageStartDate);
        String currentStage = resolveCurrentStage(nodes, currentIndex, hasProgress, completedFlow);
        String completedActions = resolveCompletedActions(nodes, currentIndex, hasProgress, completedNode, completedFlow);
        String nextAction = resolveNextAction(nodes, currentIndex, hasProgress, completedFlow);
        String nextActionRule = resolveNextActionRule(nodes, currentIndex, hasProgress, completedFlow);

        return new StudentPartyFlowItemResponse(
                flow.getId(),
                flow.getFlowCode(),
                flow.getFlowName(),
                flow.getFlowType(),
                flow.getIsActive() != null && flow.getIsActive() == 1,
                hasProgress,
                currentStage,
                progressPercent,
                stageStartDate,
                nextDeadline,
                completedActions,
                nextAction,
                nextActionRule,
                stages
        );
    }

    private List<StudentPartyFlowStageResponse> buildStages(List<LatestPartyFlowNode> nodes,
                                                            int currentIndex,
                                                            boolean completedNode,
                                                            boolean completedFlow) {
        return nodes.stream()
                .sorted(Comparator.comparing(LatestPartyFlowNode::getSeqNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(node -> {
                    int index = nodes.indexOf(node);
                    boolean completed = completedFlow
                            || (currentIndex >= 0 && index < currentIndex)
                            || (completedNode && currentIndex >= 0 && index == currentIndex);
                    boolean current = !completedFlow && !completedNode && currentIndex >= 0 && index == currentIndex;
                    String status = completed ? "completed" : current ? "in_progress" : "pending";
                    return new StudentPartyFlowStageResponse(
                            node.getId(),
                            node.getSeqNo(),
                            node.getNodeName(),
                            node.getDescription(),
                            node.getExpectedDays(),
                            status,
                            getStatusText(status),
                            completed ? "completed" : "incomplete",
                            completed,
                            current
                    );
                })
                .toList();
    }

    private int calcProgressPercent(List<StudentPartyFlowStageResponse> stages, boolean hasProgress, boolean completedFlow) {
        if (stages.isEmpty() || !hasProgress) {
            return 0;
        }
        if (completedFlow) {
            return 100;
        }
        long completedCount = stages.stream().filter(StudentPartyFlowStageResponse::completed).count();
        return Math.round((completedCount * 100.0f) / stages.size());
    }

    private int resolveCurrentIndex(LatestPartyStudentProgress progress, List<LatestPartyFlowNode> nodes) {
        if (progress == null || nodes.isEmpty()) {
            return -1;
        }
        if (progress.getCurrentNodeId() != null) {
            for (int i = 0; i < nodes.size(); i++) {
                if (progress.getCurrentNodeId().equals(nodes.get(i).getId())) {
                    return i;
                }
            }
        }
        String status = normalizeStatus(progress.getStatus());
        if ("completed".equals(status)) {
            return nodes.size() - 1;
        }
        return 0;
    }

    private boolean isCompleted(LatestPartyStudentProgress progress) {
        return progress != null && "completed".equals(normalizeStatus(progress.getStatus()));
    }

    private boolean isCompletedFlow(LatestPartyStudentProgress progress, List<LatestPartyFlowNode> nodes, int currentIndex) {
        return isCompleted(progress) && (progress.getCurrentNodeId() == null || currentIndex == nodes.size() - 1);
    }

    private LocalDate resolveStageStartDate(LatestPartyStudentProgress progress) {
        if (progress == null) {
            return null;
        }
        LocalDateTime startedAt = progress.getStartedAt() == null ? progress.getCreatedAt() : progress.getStartedAt();
        return startedAt == null ? null : startedAt.toLocalDate();
    }

    private LocalDate resolveNextDeadline(LatestPartyStudentProgress progress,
                                          List<LatestPartyFlowNode> nodes,
                                          int currentIndex,
                                          LocalDate stageStartDate) {
        if (progress != null && progress.getNextDeadlineAt() != null) {
            return progress.getNextDeadlineAt().toLocalDate();
        }
        if (stageStartDate == null || currentIndex < 0 || currentIndex >= nodes.size()) {
            return null;
        }
        Integer expectedDays = nodes.get(currentIndex).getExpectedDays();
        return expectedDays == null ? null : stageStartDate.plusDays(expectedDays);
    }

    private String resolveCurrentStage(List<LatestPartyFlowNode> nodes,
                                       int currentIndex,
                                       boolean hasProgress,
                                       boolean completedFlow) {
        if (nodes.isEmpty()) {
            return hasProgress ? "暂无节点配置" : "未开启";
        }
        if (!hasProgress) {
            return "未开启";
        }
        if (completedFlow) {
            return nodes.get(nodes.size() - 1).getNodeName();
        }
        if (currentIndex >= 0 && currentIndex < nodes.size()) {
            return nodes.get(currentIndex).getNodeName();
        }
        return nodes.get(0).getNodeName();
    }

    private String resolveCompletedActions(List<LatestPartyFlowNode> nodes,
                                           int currentIndex,
                                           boolean hasProgress,
                                           boolean completedNode,
                                           boolean completedFlow) {
        if (!hasProgress) {
            return "当前账号尚未关联该流程";
        }
        if (completedFlow) {
            return "当前流程已完成";
        }
        if (nodes.isEmpty() || currentIndex <= 0) {
            return completedNode ? "已完成当前流程阶段" : "已进入当前流程阶段";
        }
        int completedEndExclusive = completedNode ? currentIndex + 1 : currentIndex;
        return "已完成 " + nodes.subList(0, completedEndExclusive).stream()
                .map(LatestPartyFlowNode::getNodeName)
                .filter(name -> name != null && !name.isBlank())
                .reduce((left, right) -> left + "；" + right)
                .orElse("前置节点");
    }

    private String resolveNextAction(List<LatestPartyFlowNode> nodes,
                                     int currentIndex,
                                     boolean hasProgress,
                                     boolean completedFlow) {
        if (!hasProgress) {
            return "请等待管理端开通该流程或同步真实进度记录";
        }
        if (completedFlow) {
            return "当前流程已完成，请关注后续通知";
        }
        if (nodes.isEmpty()) {
            return "暂无节点配置";
        }
        int targetIndex = currentIndex >= 0 && currentIndex < nodes.size() ? currentIndex : 0;
        String description = nodes.get(targetIndex).getDescription();
        return (description == null || description.isBlank()) ? "请按节点要求继续推进" : description;
    }

    private String resolveNextActionRule(List<LatestPartyFlowNode> nodes,
                                        int currentIndex,
                                        boolean hasProgress,
                                        boolean completedFlow) {
        if (!hasProgress) {
            return "当前流程未开通";
        }
        if (completedFlow) {
            return "流程已完成";
        }
        if (nodes.isEmpty()) {
            return "暂无节点规则";
        }
        int targetIndex = currentIndex >= 0 && currentIndex < nodes.size() ? currentIndex : 0;
        Integer expectedDays = nodes.get(targetIndex).getExpectedDays();
        if (expectedDays != null) {
            return "预计 " + expectedDays + " 天内完成当前节点";
        }
        return "按管理端配置推进";
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "completed" -> "已完成";
            case "in_progress" -> "进行中";
            default -> "未完成";
        };
    }

    private String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "completed", "in_progress", "paused", "not_started" -> value;
            default -> "not_started";
        };
    }

    private Long currentStudentId() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return user.studentId() != null ? user.studentId() : user.userId();
    }
}
