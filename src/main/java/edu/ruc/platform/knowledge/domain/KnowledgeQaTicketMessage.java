package edu.ruc.platform.knowledge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "kb_qa_ticket_message")
public class KnowledgeQaTicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ticketId;

    @Column(length = 64)
    private String actorName;

    @Column(length = 32)
    private String actorRole;

    @Column(nullable = false)
    private String messageText;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

