package edu.ruc.platform.student.service;

import edu.ruc.platform.common.mock.MockDataStore;
import edu.ruc.platform.party.dto.StudentPartyFlowItemResponse;
import edu.ruc.platform.party.dto.StudentPartyFlowStageResponse;
import edu.ruc.platform.party.dto.StudentPartyFlowStateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockStudentPartyFlowService implements StudentPartyFlowApplicationService {

    private final MockDataStore mockDataStore;

    @Override
    public StudentPartyFlowStateResponse getCurrentStudentFlowState() {
        var progress = mockDataStore.partyProgress();
        var timeline = mockDataStore.partyTimeline();
        List<StudentPartyFlowStageResponse> stages = timeline.stages().stream()
                .map(stage -> new StudentPartyFlowStageResponse(
                        null,
                        null,
                        stage.stageName(),
                        stage.description(),
                        stage.expectedDurationDays(),
                        stage.completed() ? "completed" : stage.current() ? "in_progress" : "pending",
                        stage.completed() ? "已完成" : stage.current() ? "进行中" : "未完成",
                        stage.completed() ? "completed" : "incomplete",
                        stage.completed(),
                        stage.current()
                ))
                .toList();

        List<StudentPartyFlowItemResponse> flows = List.of(new StudentPartyFlowItemResponse(
                1L,
                "PARTY_JOIN",
                "入党流程",
                "PARTY",
                true,
                true,
                progress.currentStage(),
                100,
                progress.stageStartDate(),
                progress.nextDeadline(),
                progress.completedActions(),
                progress.nextAction(),
                progress.nextActionRule(),
                stages
        ));

        return new StudentPartyFlowStateResponse(
                true,
                "您暂未开启任何党团流程",
                "当前账号在 Mock 数据中尚未关联到任何党团流程记录",
                "学生端仅支持查看，流程状态由管理端教师审核更新。",
                flows
        );
    }
}
