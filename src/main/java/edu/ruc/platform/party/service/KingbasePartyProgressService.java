package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import edu.ruc.platform.party.domain.LatestPartyReminderTask;
import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyReminderTaskRepository;
import edu.ruc.platform.party.repository.LatestPartyStudentProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbasePartyProgressService implements PartyProgressApplicationService {

    private final LatestPartyStudentProgressRepository latestPartyStudentProgressRepository;
    private final LatestPartyFlowNodeRepository latestPartyFlowNodeRepository;
    private final LatestPartyReminderTaskRepository latestPartyReminderTaskRepository;

    @Override
    public PartyProgressResponse getProgress(Long studentId) {
        return buildProgressResponse(resolveCurrentProgress(studentId));
    }

    @Override
    public PartyStageTimelineResponse getTimeline(Long studentId) {
        PartyProgressResponse progress = getProgress(studentId);
        return new PartyStageTimelineResponse(
                studentId,
                progress.currentStage(),
                List.of(
                        new PartyStageTimelineResponse.StageNode("入党申请人", true, false, progress.stageStartDate().minusMonths(3), progress.stageStartDate().minusMonths(2), 30, "提交申请书后完成基础登记", "提交申请书并完成基础登记"),
                        new PartyStageTimelineResponse.StageNode("党课学习小组", true, false, progress.stageStartDate().minusMonths(1), progress.stageStartDate(), 30, "完成党课学习与考核", "完成党课学习小组课程"),
                        new PartyStageTimelineResponse.StageNode(progress.currentStage(), true, true, progress.stageStartDate(), progress.nextDeadline(), 90, progress.nextActionRule(), progress.completedActions()),
                        new PartyStageTimelineResponse.StageNode("发展对象", false, false, null, progress.nextDeadline().plusMonths(3), 90, "按学期节点完成推优与答辩", "满足培养期后按学期节点推进"),
                        new PartyStageTimelineResponse.StageNode("预备党员", false, false, null, null, 180, "支部审批通过后进入预备期", "等待支部审批"),
                        new PartyStageTimelineResponse.StageNode("正式党员", false, false, null, null, 365, "预备期满后转正", "转正完成")
                )
        );
    }

    @Override
    public List<ReminderResponse> listReminders(Long studentId) {
        LocalDate today = LocalDate.now();
        List<LatestPartyStudentProgress> progresses = latestPartyStudentProgressRepository.findAllByStudentUserIdAndIsDeleted(studentId, 0);
        if (progresses.isEmpty()) {
            throw new BusinessException("未找到党团流程记录");
        }
        return progresses.stream()
                .sorted(this::compareProgress)
                .flatMap(progress -> buildReminderResponses(progress, today).stream())
                .sorted(Comparator.comparing(ReminderResponse::remindDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private LatestPartyStudentProgress resolveCurrentProgress(Long studentId) {
        return latestPartyStudentProgressRepository.findAllByStudentUserIdAndIsDeleted(studentId, 0).stream()
                .min(this::compareProgress)
                .orElseThrow(() -> new BusinessException("未找到党团流程记录"));
    }

    private PartyProgressResponse buildProgressResponse(LatestPartyStudentProgress progress) {
        LatestPartyFlowNode node = progress.getCurrentNodeId() == null ? null
                : latestPartyFlowNodeRepository.findById(progress.getCurrentNodeId()).orElse(null);
        LocalDate stageStartDate = resolveStageStartDate(progress);
        LocalDate nextDeadline = resolveNextDeadline(progress);
        return new PartyProgressResponse(
                node == null ? resolveFallbackStage(progress) : node.getNodeName(),
                stageStartDate,
                (int) ChronoUnit.DAYS.between(stageStartDate, LocalDate.now()),
                nextDeadline,
                buildCompletedActions(progress, node),
                buildNextAction(progress, node),
                buildNextActionRule(progress, node)
        );
    }

    private List<ReminderResponse> buildReminderResponses(LatestPartyStudentProgress progress, LocalDate today) {
        List<LatestPartyReminderTask> tasks = latestPartyReminderTaskRepository.findByProgressIdOrderByDueAtAsc(progress.getId());
        if (tasks.isEmpty()) {
            PartyProgressResponse current = buildProgressResponse(progress);
            return List.of(new ReminderResponse(
                    "后续动作提醒",
                    current.nextAction(),
                    current.nextDeadline(),
                    "MEDIUM",
                    current.currentStage(),
                    current.nextActionRule(),
                    (int) ChronoUnit.DAYS.between(today, current.nextDeadline()),
                    current.nextDeadline().isBefore(today),
                    null,
                    "miniprogram",
                    "generated"
            ));
        }
        return tasks.stream()
                .map(task -> buildTaskReminderResponse(progress, task, today))
                .toList();
    }

    private ReminderResponse buildTaskReminderResponse(LatestPartyStudentProgress progress,
                                                       LatestPartyReminderTask task,
                                                       LocalDate today) {
        LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(task.getNodeId()).orElse(null);
        LocalDate remindDate = task.getDueAt().toLocalDate();
        return new ReminderResponse(
                "流程提醒",
                node == null ? "请按节点要求推进党团流程" : node.getNodeName(),
                remindDate,
                resolveLevel(task),
                node == null ? resolveFallbackStage(progress) : node.getNodeName(),
                task.getStatus(),
                (int) ChronoUnit.DAYS.between(today, remindDate),
                remindDate.isBefore(today),
                task.getId(),
                task.getChannel(),
                task.getStatus()
        );
    }

    private int compareProgress(LatestPartyStudentProgress left, LatestPartyStudentProgress right) {
        return Comparator
                .comparingInt(this::statusPriority)
                .thenComparing(LatestPartyStudentProgress::getUpdatedNodeAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(LatestPartyStudentProgress::getNextDeadlineAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(LatestPartyStudentProgress::getId, Comparator.nullsLast(Comparator.reverseOrder()))
                .compare(left, right);
    }

    private int statusPriority(LatestPartyStudentProgress progress) {
        return switch (normalizeStatus(progress.getStatus())) {
            case "in_progress" -> 0;
            case "paused" -> 1;
            case "not_started" -> 2;
            case "completed" -> 3;
            default -> 4;
        };
    }

    private LocalDate resolveStageStartDate(LatestPartyStudentProgress progress) {
        LocalDateTime startedAt = progress.getStartedAt() == null ? progress.getCreatedAt() : progress.getStartedAt();
        return startedAt == null ? LocalDate.now() : startedAt.toLocalDate();
    }

    private LocalDate resolveNextDeadline(LatestPartyStudentProgress progress) {
        if (progress.getNextDeadlineAt() != null) {
            return progress.getNextDeadlineAt().toLocalDate();
        }
        return resolveStageStartDate(progress).plusMonths(3);
    }

    private String buildCompletedActions(LatestPartyStudentProgress progress, LatestPartyFlowNode node) {
        if ("completed".equalsIgnoreCase(progress.getStatus())) {
            return "当前节点已完成";
        }
        return node == null ? "已进入当前阶段" : "已进入" + node.getNodeName() + "阶段";
    }

    private String buildNextAction(LatestPartyStudentProgress progress, LatestPartyFlowNode node) {
        if (node == null) {
            return "按学院通知继续推进";
        }
        return node.getDescription() == null || node.getDescription().isBlank()
                ? "按节点要求完成材料准备"
                : node.getDescription();
    }

    private String buildNextActionRule(LatestPartyStudentProgress progress, LatestPartyFlowNode node) {
        if (node == null) {
            return "按学院通知推进下一阶段";
        }
        if (node.getExpectedDays() != null) {
            return "预计 " + node.getExpectedDays() + " 天内完成当前节点";
        }
        return "按学院通知推进下一阶段";
    }

    private String resolveFallbackStage(LatestPartyStudentProgress progress) {
        return switch (normalizeStatus(progress.getStatus())) {
            case "completed" -> "正式党员";
            case "paused" -> "流程暂停";
            default -> "积极分子";
        };
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.toLowerCase();
    }

    private String resolveLevel(LatestPartyReminderTask task) {
        if ("failed".equalsIgnoreCase(task.getStatus())) {
            return "HIGH";
        }
        if ("pending".equalsIgnoreCase(task.getStatus())) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
