package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.dto.PartyFlowNodeResponse;
import edu.ruc.platform.party.dto.PartyFlowNodeUpsertRequest;
import edu.ruc.platform.party.dto.PartyFlowResponse;
import edu.ruc.platform.party.dto.PartyFlowUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockPartyFlowConfigService implements PartyFlowConfigApplicationService {

    private final AtomicLong flowIdGen = new AtomicLong(10);
    private final AtomicLong nodeIdGen = new AtomicLong(50);
    private final List<PartyFlowResponse> flows = new ArrayList<>();
    private final List<PartyFlowNodeResponse> nodes = new ArrayList<>();

    @Override
    public List<PartyFlowResponse> listFlows() {
        init();
        return List.copyOf(flows);
    }

    @Override
    public PartyFlowResponse createFlow(PartyFlowUpsertRequest request) {
        init();
        PartyFlowResponse flow = new PartyFlowResponse(
                flowIdGen.incrementAndGet(),
                request.flowCode().trim(),
                request.flowName().trim(),
                request.flowType().trim(),
                0,
                !Boolean.FALSE.equals(request.active()),
                LocalDateTime.now()
        );
        flows.add(0, flow);
        return flow;
    }

    @Override
    public PartyFlowResponse updateFlow(Long id, PartyFlowUpsertRequest request) {
        init();
        PartyFlowResponse old = flows.stream().filter(f -> f.id().equals(id)).findFirst().orElseThrow(() -> new BusinessException("流程不存在"));
        PartyFlowResponse updated = new PartyFlowResponse(
                old.id(),
                request.flowCode().trim(),
                request.flowName().trim(),
                request.flowType().trim(),
                (int) nodes.stream().filter(n -> n.flowId().equals(old.id())).count(),
                !Boolean.FALSE.equals(request.active()),
                old.createdAt()
        );
        flows.replaceAll(f -> f.id().equals(id) ? updated : f);
        return updated;
    }

    @Override
    public PartyFlowResponse copyFlow(Long id) {
        init();
        PartyFlowResponse src = flows.stream().filter(f -> f.id().equals(id)).findFirst().orElseThrow(() -> new BusinessException("流程不存在"));
        Long newId = flowIdGen.incrementAndGet();
        PartyFlowResponse flow = new PartyFlowResponse(
                newId,
                src.flowCode() + "_COPY_" + System.currentTimeMillis(),
                src.flowName() + "（复制）",
                src.flowType(),
                0,
                src.active(),
                LocalDateTime.now()
        );
        flows.add(0, flow);
        List<PartyFlowNodeResponse> srcNodes = nodes.stream().filter(n -> n.flowId().equals(src.id())).toList();
        for (PartyFlowNodeResponse n : srcNodes) {
            nodes.add(new PartyFlowNodeResponse(nodeIdGen.incrementAndGet(), newId, n.seqNo(), n.nodeName(), n.expectedDays(), n.reminderEveryDays(), n.overdueDays()));
        }
        refreshCounts();
        return flows.stream().filter(f -> f.id().equals(newId)).findFirst().orElse(flow);
    }

    @Override
    public void deleteFlow(Long id) {
        init();
        boolean removed = flows.removeIf(f -> f.id().equals(id));
        nodes.removeIf(n -> n.flowId().equals(id));
        if (!removed) {
            throw new BusinessException("流程不存在");
        }
        refreshCounts();
    }

    @Override
    public List<PartyFlowNodeResponse> listFlowNodes(Long flowId) {
        init();
        return nodes.stream().filter(n -> n.flowId().equals(flowId)).sorted(java.util.Comparator.comparing(PartyFlowNodeResponse::seqNo)).toList();
    }

    @Override
    public PartyFlowNodeResponse createFlowNode(Long flowId, PartyFlowNodeUpsertRequest request) {
        init();
        boolean exists = flows.stream().anyMatch(f -> f.id().equals(flowId));
        if (!exists) {
            throw new BusinessException("流程不存在");
        }
        PartyFlowNodeResponse node = new PartyFlowNodeResponse(
                nodeIdGen.incrementAndGet(),
                flowId,
                request.seqNo(),
                request.nodeName().trim(),
                request.expectedDays(),
                request.reminderEveryDays(),
                request.overdueDays()
        );
        nodes.add(node);
        normalizeSeq(flowId);
        refreshCounts();
        return node;
    }

    @Override
    public PartyFlowNodeResponse updateFlowNode(Long nodeId, PartyFlowNodeUpsertRequest request) {
        init();
        PartyFlowNodeResponse old = nodes.stream().filter(n -> n.id().equals(nodeId)).findFirst().orElseThrow(() -> new BusinessException("节点不存在"));
        PartyFlowNodeResponse updated = new PartyFlowNodeResponse(
                old.id(),
                old.flowId(),
                request.seqNo(),
                request.nodeName().trim(),
                request.expectedDays(),
                request.reminderEveryDays(),
                request.overdueDays()
        );
        nodes.replaceAll(n -> n.id().equals(nodeId) ? updated : n);
        normalizeSeq(updated.flowId());
        refreshCounts();
        return updated;
    }

    @Override
    public List<PartyFlowNodeResponse> moveFlowNode(Long nodeId, String direction) {
        init();
        PartyFlowNodeResponse node = nodes.stream().filter(n -> n.id().equals(nodeId)).findFirst().orElseThrow(() -> new BusinessException("节点不存在"));
        String dir = String.valueOf(direction).trim().toLowerCase(Locale.ROOT);
        List<PartyFlowNodeResponse> list = listFlowNodes(node.flowId());
        int idx = -1;
        for (int i = 0; i < list.size(); i += 1) {
            if (list.get(i).id().equals(nodeId)) {
                idx = i;
                break;
            }
        }
        int swapWith = dir.equals("up") ? idx - 1 : dir.equals("down") ? idx + 1 : idx;
        if (idx < 0 || swapWith < 0 || swapWith >= list.size() || swapWith == idx) {
            return list;
        }
        PartyFlowNodeResponse a = list.get(idx);
        PartyFlowNodeResponse b = list.get(swapWith);
        nodes.replaceAll(n -> {
            if (n.id().equals(a.id())) {
                return new PartyFlowNodeResponse(n.id(), n.flowId(), b.seqNo(), n.nodeName(), n.expectedDays(), n.reminderEveryDays(), n.overdueDays());
            }
            if (n.id().equals(b.id())) {
                return new PartyFlowNodeResponse(n.id(), n.flowId(), a.seqNo(), n.nodeName(), n.expectedDays(), n.reminderEveryDays(), n.overdueDays());
            }
            return n;
        });
        normalizeSeq(node.flowId());
        refreshCounts();
        return listFlowNodes(node.flowId());
    }

    @Override
    public void deleteFlowNode(Long nodeId) {
        init();
        PartyFlowNodeResponse node = nodes.stream().filter(n -> n.id().equals(nodeId)).findFirst().orElseThrow(() -> new BusinessException("节点不存在"));
        nodes.removeIf(n -> n.id().equals(nodeId));
        normalizeSeq(node.flowId());
        refreshCounts();
    }

    private void init() {
        if (!flows.isEmpty()) {
            return;
        }
        flows.addAll(List.of(
                new PartyFlowResponse(1L, "PARTY_001", "入党流程", "party", 5, true, LocalDateTime.of(2023, 9, 1, 0, 0)),
                new PartyFlowResponse(2L, "PARTY_002", "入团流程", "league", 4, true, LocalDateTime.of(2023, 9, 1, 0, 0)),
                new PartyFlowResponse(3L, "PARTY_003", "党员转正流程", "party", 3, true, LocalDateTime.of(2023, 10, 15, 0, 0))
        ));
        nodes.addAll(List.of(
                new PartyFlowNodeResponse(11L, 1L, 1, "递交入党申请书", 1, 3, 7),
                new PartyFlowNodeResponse(12L, 1L, 2, "成为入党积极分子", 180, 7, 7),
                new PartyFlowNodeResponse(13L, 1L, 3, "参加院校培训", 30, 7, 7),
                new PartyFlowNodeResponse(14L, 1L, 4, "成为预备党员", 365, 14, 7)
        ));
        refreshCounts();
        flowIdGen.set(100);
        nodeIdGen.set(200);
    }

    private void refreshCounts() {
        flows.replaceAll(f -> new PartyFlowResponse(
                f.id(),
                f.flowCode(),
                f.flowName(),
                f.flowType(),
                (int) nodes.stream().filter(n -> n.flowId().equals(f.id())).count(),
                f.active(),
                f.createdAt()
        ));
    }

    private void normalizeSeq(Long flowId) {
        List<PartyFlowNodeResponse> list = nodes.stream().filter(n -> n.flowId().equals(flowId)).sorted(java.util.Comparator.comparing(PartyFlowNodeResponse::seqNo)).toList();
        for (int i = 0; i < list.size(); i += 1) {
            PartyFlowNodeResponse n = list.get(i);
            int seq = i + 1;
            if (n.seqNo() != seq) {
                nodes.replaceAll(it -> it.id().equals(n.id())
                        ? new PartyFlowNodeResponse(it.id(), it.flowId(), seq, it.nodeName(), it.expectedDays(), it.reminderEveryDays(), it.overdueDays())
                        : it);
            }
        }
    }
}

