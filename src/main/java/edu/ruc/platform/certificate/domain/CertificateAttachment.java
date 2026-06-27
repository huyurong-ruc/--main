package edu.ruc.platform.certificate.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "certificate_attachment")
public class CertificateAttachment extends BaseEntity {

    @Column(nullable = false)
    private Long requestId;

    @Column(nullable = false)
    private Long fileId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    @Column
    private Long fileSize;

    @Column(length = 500)
    private String storagePath;
}