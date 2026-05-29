package edu.ruc.platform.certificate.dto;

import java.util.List;

public record CertificateRequestResponse(
        Long id,
        Long studentId,
        String certificateType,
        String status,
        String generatedPdfPath,
        List<CertificateAttachmentResponse> attachments
) {
}
