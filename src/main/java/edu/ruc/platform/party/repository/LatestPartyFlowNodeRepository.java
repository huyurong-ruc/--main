package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestPartyFlowNodeRepository extends JpaRepository<LatestPartyFlowNode, Long> {

    List<LatestPartyFlowNode> findByFlowIdAndIsDeletedOrderBySeqNoAsc(Long flowId, Integer isDeleted);
}
