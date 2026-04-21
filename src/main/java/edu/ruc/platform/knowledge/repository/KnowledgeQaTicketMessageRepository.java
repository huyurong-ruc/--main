package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.KnowledgeQaTicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeQaTicketMessageRepository extends JpaRepository<KnowledgeQaTicketMessage, Long> {

    List<KnowledgeQaTicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}

