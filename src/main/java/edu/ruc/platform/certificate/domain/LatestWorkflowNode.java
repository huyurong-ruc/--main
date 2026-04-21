package edu.ruc.platform.certificate.domain;

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
@Table(name = "wf_node")
public class LatestWorkflowNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long wfId;

    @Column(nullable = false)
    private Integer seqNo;

    @Column(nullable = false, length = 128)
    private String nodeName;

    @Column(length = 32)
    private String approverRole;

    @Column
    private Integer slaHours;

    @Column
    private Boolean allowReject;

    @Column
    private Long approverUserId;

    @Column(nullable = false)
    private Integer isDeleted;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
