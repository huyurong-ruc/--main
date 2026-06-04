package edu.ruc.platform.certificate.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApprovalTaskResponse(
        Long requestId,
        Long studentId,
        String studentName,
        String certificateType,
        String status,
        String reason,
        List<CertificateAttachmentResponse> attachments,
        LocalDateTime submittedAt
) {
}
