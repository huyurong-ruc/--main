package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.LatestPartyFlow;
import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import edu.ruc.platform.party.dto.PartyFlowNodeResponse;
import edu.ruc.platform.party.dto.PartyFlowNodeUpsertRequest;
import edu.ruc.platform.party.dto.PartyFlowResponse;
import edu.ruc.platform.party.dto.PartyFlowUpsertRequest;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyFlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PartyFlowConfigService implements PartyFlowConfigApplicationService {

    private final LatestPartyFlowRepository flowRepository;
    private final LatestPartyFlowNodeRepository nodeRepository;

    @Override
    public List<PartyFlowResponse> listFlows() {
        return flowRepository.findByIsDeletedOrderByIdAsc(0).stream()
                .map(flow -> {
                    int nodeCount = nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flow.getId(), 0).size();
                    return new PartyFlowResponse(flow.getId(), flow.getFlowCode(), flow.getFlowName(), flow.getFlowType(), nodeCount, flow.getIsActive() == 1, flow.getCreatedAt());
                })
                .toList();
    }

    @Override
    public PartyFlowResponse createFlow(PartyFlowUpsertRequest request) {
        LatestPartyFlow flow = new LatestPartyFlow();
        flow.setFlowCode(request.flowCode().trim());
        flow.setFlowName(request.flowName().trim());
        flow.setFlowType(request.flowType().trim());
        flow.setIsActive(Boolean.FALSE.equals(request.active()) ? 0 : 1);
        flow.setIsDeleted(0);
        flow.setCreatedAt(LocalDateTime.now());
        flow.setUpdatedAt(LocalDateTime.now());
        flow = flowRepository.save(flow);
        return new PartyFlowResponse(flow.getId(), flow.getFlowCode(), flow.getFlowName(), flow.getFlowType(), 0, flow.getIsActive() == 1, flow.getCreatedAt());
    }

    @Override
    public PartyFlowResponse updateFlow(Long id, PartyFlowUpsertRequest request) {
        LatestPartyFlow flow = flowRepository.findById(id).orElseThrow(() -> new BusinessException("流程不存在"));
        flow.setFlowCode(request.flowCode().trim());
        flow.setFlowName(request.flowName().trim());
        flow.setFlowType(request.flowType().trim());
        flow.setIsActive(Boolean.FALSE.equals(request.active()) ? 0 : 1);
        flow.setUpdatedAt(LocalDateTime.now());
        flow = flowRepository.save(flow);
        int nodeCount = nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flow.getId(), 0).size();
        return new PartyFlowResponse(flow.getId(), flow.getFlowCode(), flow.getFlowName(), flow.getFlowType(), nodeCount, flow.getIsActive() == 1, flow.getCreatedAt());
    }

    @Override
    public PartyFlowResponse copyFlow(Long id) {
        LatestPartyFlow src = flowRepository.findById(id).orElseThrow(() -> new BusinessException("流程不存在"));
        LatestPartyFlow flow = new LatestPartyFlow();
        String baseCode = src.getFlowCode() == null ? "FLOW" : src.getFlowCode();
        flow.setFlowCode(baseCode + "_COPY_" + System.currentTimeMillis());
        flow.setFlowName((src.getFlowName() == null ? baseCode : src.getFlowName()) + "（复制）");
        flow.setFlowType(src.getFlowType());
        flow.setIsActive(src.getIsActive());
        flow.setIsDeleted(0);
        flow.setCreatedAt(LocalDateTime.now());
        flow.setUpdatedAt(LocalDateTime.now());
        flow = flowRepository.save(flow);

        List<LatestPartyFlowNode> nodes = nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(src.getId(), 0);
        for (LatestPartyFlowNode n : nodes) {
            LatestPartyFlowNode cp = new LatestPartyFlowNode();
            cp.setFlowId(flow.getId());
            cp.setSeqNo(n.getSeqNo());
            cp.setNodeCode((n.getNodeCode() == null ? "NODE" : n.getNodeCode()) + "_COPY_" + System.nanoTime());
            cp.setNodeName(n.getNodeName());
            cp.setDescription(n.getDescription());
            cp.setExpectedDays(n.getExpectedDays());
            cp.setReminderOffsetDays(n.getReminderOffsetDays());
            cp.setOverdueDays(n.getOverdueDays());
            cp.setIsDeleted(0);
            cp.setCreatedAt(LocalDateTime.now());
            cp.setUpdatedAt(LocalDateTime.now());
            nodeRepository.save(cp);
        }
        return new PartyFlowResponse(flow.getId(), flow.getFlowCode(), flow.getFlowName(), flow.getFlowType(), nodes.size(), flow.getIsActive() == 1, flow.getCreatedAt());
    }

    @Override
    public void deleteFlow(Long id) {
        LatestPartyFlow flow = flowRepository.findById(id).orElseThrow(() -> new BusinessException("流程不存在"));
        flow.setIsDeleted(1);
        flow.setUpdatedAt(LocalDateTime.now());
        flowRepository.save(flow);
        nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(id, 0).forEach(n -> {
            n.setIsDeleted(1);
            n.setUpdatedAt(LocalDateTime.now());
            nodeRepository.save(n);
        });
    }

    @Override
    public List<PartyFlowNodeResponse> listFlowNodes(Long flowId) {
        return nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flowId, 0).stream()
                .map(this::toNodeResponse)
                .toList();
    }

    @Override
    public PartyFlowNodeResponse createFlowNode(Long flowId, PartyFlowNodeUpsertRequest request) {
        flowRepository.findById(flowId).orElseThrow(() -> new BusinessException("流程不存在"));
        LatestPartyFlowNode node = new LatestPartyFlowNode();
        node.setFlowId(flowId);
        node.setSeqNo(request.seqNo());
        node.setNodeCode("NODE_" + flowId + "_" + System.currentTimeMillis());
        node.setNodeName(request.nodeName().trim());
        node.setExpectedDays(request.expectedDays());
        node.setReminderOffsetDays(request.reminderEveryDays());
        node.setOverdueDays(request.overdueDays());
        node.setIsDeleted(0);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        node = nodeRepository.save(node);
        normalizeSeq(flowId);
        return toNodeResponse(node);
    }

    @Override
    public PartyFlowNodeResponse updateFlowNode(Long nodeId, PartyFlowNodeUpsertRequest request) {
        LatestPartyFlowNode node = nodeRepository.findById(nodeId).orElseThrow(() -> new BusinessException("节点不存在"));
        node.setNodeName(request.nodeName().trim());
        node.setSeqNo(request.seqNo());
        node.setExpectedDays(request.expectedDays());
        node.setReminderOffsetDays(request.reminderEveryDays());
        node.setOverdueDays(request.overdueDays());
        node.setUpdatedAt(LocalDateTime.now());
        node = nodeRepository.save(node);
        normalizeSeq(node.getFlowId());
        return toNodeResponse(node);
    }

    @Override
    public List<PartyFlowNodeResponse> moveFlowNode(Long nodeId, String direction) {
        LatestPartyFlowNode node = nodeRepository.findById(nodeId).orElseThrow(() -> new BusinessException("节点不存在"));
        String dir = String.valueOf(direction).trim().toLowerCase(Locale.ROOT);
        Long flowId = node.getFlowId();
        List<LatestPartyFlowNode> nodes = nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flowId, 0);
        int idx = -1;
        for (int i = 0; i < nodes.size(); i += 1) {
            if (Objects.equals(nodes.get(i).getId(), nodeId)) {
                idx = i;
                break;
            }
        }
        int swapWith = dir.equals("up") ? idx - 1 : dir.equals("down") ? idx + 1 : idx;
        if (idx < 0 || swapWith < 0 || swapWith >= nodes.size() || swapWith == idx) {
            return nodes.stream().map(this::toNodeResponse).toList();
        }
        LatestPartyFlowNode a = nodes.get(idx);
        LatestPartyFlowNode b = nodes.get(swapWith);
        Integer tmp = a.getSeqNo();
        a.setSeqNo(b.getSeqNo());
        b.setSeqNo(tmp);
        a.setUpdatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        nodeRepository.save(a);
        nodeRepository.save(b);
        normalizeSeq(flowId);
        return nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flowId, 0).stream().map(this::toNodeResponse).toList();
    }

    @Override
    public void deleteFlowNode(Long nodeId) {
        LatestPartyFlowNode node = nodeRepository.findById(nodeId).orElseThrow(() -> new BusinessException("节点不存在"));
        node.setIsDeleted(1);
        node.setUpdatedAt(LocalDateTime.now());
        nodeRepository.save(node);
        normalizeSeq(node.getFlowId());
    }

    private PartyFlowNodeResponse toNodeResponse(LatestPartyFlowNode node) {
        return new PartyFlowNodeResponse(
                node.getId(),
                node.getFlowId(),
                node.getSeqNo(),
                node.getNodeName(),
                node.getExpectedDays(),
                node.getReminderOffsetDays(),
                node.getOverdueDays()
        );
    }

    private void normalizeSeq(Long flowId) {
        List<LatestPartyFlowNode> nodes = nodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flowId, 0).stream()
                .sorted(Comparator.comparing(LatestPartyFlowNode::getSeqNo))
                .toList();
        for (int i = 0; i < nodes.size(); i += 1) {
            LatestPartyFlowNode n = nodes.get(i);
            int seq = i + 1;
            if (n.getSeqNo() == null || n.getSeqNo() != seq) {
                n.setSeqNo(seq);
                n.setUpdatedAt(LocalDateTime.now());
                nodeRepository.save(n);
            }
        }
    }
}

