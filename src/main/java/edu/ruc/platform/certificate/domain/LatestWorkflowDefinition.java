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
@Table(name = "wf_definition")
public class LatestWorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String wfCode;

    @Column(length = 128)
    private String wfName;

    @Column(length = 32)
    private String wfType;

    @Column(nullable = false, length = 64)
    private String businessType;

    @Column(nullable = false)
    private Integer isActive;

    @Column(nullable = false)
    private Integer isDeleted;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
