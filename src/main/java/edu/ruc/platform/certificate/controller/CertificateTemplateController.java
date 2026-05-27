package edu.ruc.platform.certificate.controller;

import edu.ruc.platform.certificate.dto.*;
import edu.ruc.platform.certificate.service.CertificateTemplateApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.security.RequireRoles;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/certificate-templates")
@RequiredArgsConstructor
public class CertificateTemplateController {

    private static final Logger log = LoggerFactory.getLogger(CertificateTemplateController.class);

    private final CertificateTemplateApplicationService templateService;
    private final ResourceLoader resourceLoader;

    @GetMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<List<CertificateTemplateResponse>> listAll() {
        return ApiResponse.success(templateService.listAll());
    }

    @GetMapping("/active")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.STUDENT})
    public ApiResponse<List<CertificateTemplateResponse>> listActive() {
        return ApiResponse.success(templateService.listActive());
    }

    @GetMapping("/type/{type}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<List<CertificateTemplateResponse>> listByType(@PathVariable String type) {
        return ApiResponse.success(templateService.listByType(type));
    }

    @GetMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<CertificateTemplateResponse> getById(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        return ApiResponse.success(templateService.getById(id));
    }

    @GetMapping("/{id}/download")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.STUDENT})
    public ResponseEntity<ByteArrayResource> download(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        CertificateTemplateResponse template = templateService.getById(id);
        String normalizedPath = normalizeTemplateFilePath(template.templateFilePath());
        String originalFileName = StringUtils.getFilename(normalizedPath);
        String downloadFileName = buildDownloadFileName(template.templateName(), originalFileName);
        Resource resource = resourceLoader.getResource("classpath:/static" + normalizedPath);

        log.info("certificate template download start: id={}, code={}, path={}",
                template.id(), template.templateCode(), normalizedPath);

        if (!resource.exists() || !resource.isReadable()) {
            log.warn("certificate template file missing: id={}, path={}", template.id(), normalizedPath);
            throw new BusinessException("模板文件不存在");
        }

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            MediaType mediaType = MediaTypeFactory.getMediaType(originalFileName)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(downloadFileName, StandardCharsets.UTF_8)
                    .build());
            headers.setContentLength(bytes.length);
            headers.setCacheControl("no-store, max-age=0");
            headers.add("X-Template-Code", template.templateCode());

            log.info("certificate template download success: id={}, code={}, bytes={}, contentType={}",
                    template.id(), template.templateCode(), bytes.length, mediaType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(bytes));
        } catch (IOException exception) {
            log.error("certificate template download failed: id={}, code={}",
                    template.id(), template.templateCode(), exception);
            throw new BusinessException("模板下载失败");
        }
    }

    @GetMapping("/code/{code}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<CertificateTemplateResponse> getByCode(@PathVariable String code) {
        return ApiResponse.success(templateService.getByCode(code));
    }

    @PostMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<CertificateTemplateResponse> create(@Valid @RequestBody CertificateTemplateCreateRequest request) {
        return ApiResponse.success("模板创建成功", templateService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<CertificateTemplateResponse> update(@Positive(message = "模板ID必须大于0") @PathVariable Long id,
                                                           @Valid @RequestBody CertificateTemplateUpdateRequest request) {
        return ApiResponse.success("模板更新成功", templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<Void> delete(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        templateService.delete(id);
        return ApiResponse.success("模板删除成功", null);
    }

    @GetMapping("/{id}/preview/student/{studentId}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<String> renderPreview(@Positive(message = "模板ID必须大于0") @PathVariable Long id,
                                            @Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(templateService.renderPreview(id, studentId));
    }

    private String normalizeTemplateFilePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new BusinessException("模板文件路径缺失");
        }

        String normalizedPath = filePath.trim().replace('\\', '/');
        if (!normalizedPath.startsWith("/templates/") || normalizedPath.contains("..")) {
            throw new BusinessException("模板文件路径非法");
        }

        return normalizedPath;
    }

    private String buildDownloadFileName(String templateName, String originalFileName) {
        String safeOriginalName = StringUtils.hasText(originalFileName) ? originalFileName : "template.pdf";
        String extension = "";
        int extensionIndex = safeOriginalName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = safeOriginalName.substring(extensionIndex);
        }

        String baseName = StringUtils.hasText(templateName) ? templateName.trim() : safeOriginalName;
        if (StringUtils.hasText(extension) && !baseName.endsWith(extension)) {
            return baseName + extension;
        }
        return baseName;
    }
}
