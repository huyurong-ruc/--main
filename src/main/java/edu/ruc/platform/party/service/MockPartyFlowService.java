package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.dto.PartyFlowStageCreateRequest;
import edu.ruc.platform.party.dto.PartyFlowStageResponse;
import edu.ruc.platform.party.dto.PartyFlowTemplateCreateRequest;
import edu.ruc.platform.party.dto.PartyFlowTemplateResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockPartyFlowService implements PartyFlowApplicationService {

    private final AtomicLong flowIdGenerator = new AtomicLong(1);
    private final AtomicLong stageIdGenerator = new AtomicLong(1);

    private final Map<Long, PartyFlowTemplateResponse> templatesById = new ConcurrentHashMap<>();
    private final Map<Long, List<PartyFlowStageResponse>> stagesByFlowId = new ConcurrentHashMap<>();

    @Override
    public List<PartyFlowTemplateResponse> listAllTemplates() {
        return templatesById.values().stream()
                .sorted(Comparator.comparing(PartyFlowTemplateResponse::id))
                .toList();
    }

    @Override
    public PartyFlowTemplateResponse getTemplateById(Long id) {
        PartyFlowTemplateResponse item = templatesById.get(id);
        if (item == null) {
            throw new BusinessException("流程模板不存在");
        }
        return item;
    }

    @Override
    public PartyFlowTemplateResponse createTemplate(PartyFlowTemplateCreateRequest request) {
        Long id = flowIdGenerator.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        PartyFlowTemplateResponse created = new PartyFlowTemplateResponse(
                id,
                request.flowCode(),
                request.flowName(),
                request.flowType(),
                request.totalStages(),
                request.description(),
                true,
                List.of(),
                now
        );
        templatesById.put(id, created);
        stagesByFlowId.put(id, new ArrayList<>());
        return created;
    }

    @Override
    public PartyFlowStageResponse addStage(Long flowId, PartyFlowStageCreateRequest request) {
        PartyFlowTemplateResponse existing = templatesById.get(flowId);
        if (existing == null) {
            throw new BusinessException("流程模板不存在");
        }
        Long stageId = stageIdGenerator.incrementAndGet();
        PartyFlowStageResponse stage = new PartyFlowStageResponse(
                stageId,
                request.seqNo(),
                request.stageCode(),
                request.stageName(),
                request.description(),
                request.requiredMaterials(),
                request.estimatedDays(),
                request.reminderDaysBefore(),
                true
        );
        stagesByFlowId.computeIfAbsent(flowId, key -> new ArrayList<>()).add(stage);
        List<PartyFlowStageResponse> stages = stagesByFlowId.get(flowId).stream()
                .sorted(Comparator.comparingInt(PartyFlowStageResponse::seqNo))
                .toList();
        templatesById.put(flowId, new PartyFlowTemplateResponse(
                existing.id(),
                existing.flowCode(),
                existing.flowName(),
                existing.flowType(),
                existing.totalStages(),
                existing.description(),
                existing.isActive(),
                stages,
                existing.createdAt()
        ));
        return stage;
    }

    @Override
    public void deleteTemplate(Long id) {
        if (!templatesById.containsKey(id)) {
            throw new BusinessException("流程模板不存在");
        }
        templatesById.remove(id);
        stagesByFlowId.remove(id);
    }
}
