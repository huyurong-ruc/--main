package edu.ruc.platform.platform.dto;

public record PlatformFileDownloadPayload(
        Long id,
        String fileName,
        String contentType,
        Long fileSize,
        byte[] bytes
) {
}

