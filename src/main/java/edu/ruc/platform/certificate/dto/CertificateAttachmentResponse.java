package edu.ruc.platform.certificate.dto;

public record CertificateAttachmentResponse(
        Long id,
        Long fileId,
        String fileName,
        String contentType,
        Long fileSize,
        String storagePath
) {
}