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
@Table(name = "cert_template")
public class LatestCertTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String templateCode;

    @Column(nullable = false, length = 128)
    private String templateName;

    @Column(nullable = false)
    private Long fileId;

    @Column(nullable = false, length = 16)
    private String outputFormat;

    @Column(nullable = false)
    private Integer isActive;

    @Column
    private Long createdBy;

    @Column(name = "ext_json")
    private String extJson;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer isDeleted;
}
