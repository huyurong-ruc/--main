package edu.ruc.platform.party.domain;

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
@Table(name = "party_flow")
public class LatestPartyFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String flowCode;

    @Column(nullable = false, length = 128)
    private String flowName;

    @Column(nullable = false, length = 32)
    private String flowType;

    @Column(nullable = false)
    private Integer isActive;

    @Column(nullable = false)
    private Integer isDeleted;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}

