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
@Table(name = "kb_qa_ticket")
public class KnowledgeQaTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long askUserId;

    @Column(length = 64)
    private String askUsername;

    @Column(length = 64)
    private String askName;

    @Column(nullable = false)
    private String questionText;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private Long matchedFaqId;

    @Column
    private Long handledBy;

    @Column
    private LocalDateTime handledAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

