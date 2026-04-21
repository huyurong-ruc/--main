package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.LatestPartyFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestPartyFlowRepository extends JpaRepository<LatestPartyFlow, Long> {

    List<LatestPartyFlow> findByIsDeletedOrderByIdAsc(Integer isDeleted);
}

