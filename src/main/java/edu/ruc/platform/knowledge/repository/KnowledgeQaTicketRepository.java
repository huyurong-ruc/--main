package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.KnowledgeQaTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeQaTicketRepository extends JpaRepository<KnowledgeQaTicket, Long> {

    List<KnowledgeQaTicket> findByStatusOrderByCreatedAtDesc(String status);

    List<KnowledgeQaTicket> findByAskUserIdOrderByCreatedAtDesc(Long askUserId);

    List<KnowledgeQaTicket> findByAskUserIdAndStatusOrderByCreatedAtDesc(Long askUserId, String status);
}
