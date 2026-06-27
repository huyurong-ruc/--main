package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.CertificateAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateAttachmentRepository extends JpaRepository<CertificateAttachment, Long> {

    List<CertificateAttachment> findByRequestId(Long requestId);

    void deleteByRequestId(Long requestId);
}