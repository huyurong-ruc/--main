package edu.ruc.platform.party.service;

import edu.ruc.platform.party.dto.PartyFlowNodeResponse;
import edu.ruc.platform.party.dto.PartyFlowNodeUpsertRequest;
import edu.ruc.platform.party.dto.PartyFlowResponse;
import edu.ruc.platform.party.dto.PartyFlowUpsertRequest;

import java.util.List;

public interface PartyFlowConfigApplicationService {

    List<PartyFlowResponse> listFlows();

    PartyFlowResponse createFlow(PartyFlowUpsertRequest request);

    PartyFlowResponse updateFlow(Long id, PartyFlowUpsertRequest request);

    PartyFlowResponse copyFlow(Long id);

    void deleteFlow(Long id);

    List<PartyFlowNodeResponse> listFlowNodes(Long flowId);

    PartyFlowNodeResponse createFlowNode(Long flowId, PartyFlowNodeUpsertRequest request);

    PartyFlowNodeResponse updateFlowNode(Long nodeId, PartyFlowNodeUpsertRequest request);

    List<PartyFlowNodeResponse> moveFlowNode(Long nodeId, String direction);

    void deleteFlowNode(Long nodeId);
}

