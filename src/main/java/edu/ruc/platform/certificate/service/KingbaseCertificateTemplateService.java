package edu.ruc.platform.certificate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ruc.platform.certificate.dto.CertificateTemplateCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateTemplateResponse;
import edu.ruc.platform.certificate.dto.CertificateTemplateUpdateRequest;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.knowledge.domain.LatestCertTemplate;
import edu.ruc.platform.knowledge.domain.LatestFileObject;
import edu.ruc.platform.knowledge.repository.LatestCertTemplateRepository;
import edu.ruc.platform.knowledge.repository.LatestFileObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseCertificateTemplateService implements CertificateTemplateApplicationService {

    private final LatestCertTemplateRepository latestCertTemplateRepository;
    private final LatestFileObjectRepository latestFileObjectRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<CertificateTemplateResponse> listAll() {
        return latestCertTemplateRepository.findByIsDeletedOrderByIdAsc(0).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CertificateTemplateResponse> listActive() {
        return latestCertTemplateRepository.findByIsDeletedAndIsActive(0, 1).stream()
                .sorted(Comparator.comparing(LatestCertTemplate::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CertificateTemplateResponse> listByType(String certificateType) {
        String normalized = StringUtils.hasText(certificateType) ? certificateType.trim() : null;
        if (normalized == null) {
            return listActive();
        }
        return listActive().stream()
                .filter(item -> StringUtils.hasText(item.certificateType()) && item.certificateType().equalsIgnoreCase(normalized))
                .toList();
    }

    @Override
    public CertificateTemplateResponse getById(Long id) {
        LatestCertTemplate template = latestCertTemplateRepository.findById(id)
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        return toResponse(template);
    }

    @Override
    public CertificateTemplateResponse getByCode(String templateCode) {
        String normalized = StringUtils.hasText(templateCode) ? templateCode.trim() : "";
        if (normalized.isEmpty()) {
            throw new BusinessException("模板编码不能为空");
        }
        LatestCertTemplate template = latestCertTemplateRepository.findByTemplateCodeAndIsDeleted(normalized, 0)
                .orElseThrow(() -> new BusinessException("模板不存在: " + normalized));
        return toResponse(template);
    }

    @Override
    public CertificateTemplateResponse create(CertificateTemplateCreateRequest request) {
        throw new BusinessException("kingbase 环境不支持通过该接口维护模板，请使用后台模板管理接口");
    }

    @Override
    public CertificateTemplateResponse update(Long id, CertificateTemplateUpdateRequest request) {
        throw new BusinessException("kingbase 环境不支持通过该接口维护模板，请使用后台模板管理接口");
    }

    @Override
    public void delete(Long id) {
        throw new BusinessException("kingbase 环境不支持通过该接口维护模板，请使用后台模板管理接口");
    }

    @Override
    public String renderPreview(Long templateId, Long studentId) {
        throw new BusinessException("kingbase 环境暂不支持模板预览");
    }

    private CertificateTemplateResponse toResponse(LatestCertTemplate template) {
        LatestFileObject fileObject = template.getFileId() == null
                ? null
                : latestFileObjectRepository.findById(template.getFileId())
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .orElse(null);
        String certificateType = extractCertificateType(template.getExtJson(), template.getTemplateName());
        String outputFormat = normalizeOutputFormat(template.getOutputFormat(), fileObject == null ? null : fileObject.getOriginalName());
        String description = buildDescription(certificateType);

        return new CertificateTemplateResponse(
                template.getId(),
                template.getTemplateCode(),
                template.getTemplateName(),
                certificateType,
                "",
                fileObject == null ? null : fileObject.getStoragePath(),
                outputFormat,
                template.getIsActive() != null && template.getIsActive() == 1,
                description,
                null,
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    private String buildDescription(String certificateType) {
        if (!StringUtils.hasText(certificateType)) {
            return "用于证明模板下载";
        }
        return "用于" + certificateType.trim() + "模板下载";
    }

    private String extractCertificateType(String extJson, String templateName) {
        String parsed = extractExtField(extJson, "certificateType");
        if (StringUtils.hasText(parsed)) {
            return parsed.trim();
        }
        return deriveCertificateTypeFromName(templateName);
    }

    private String deriveCertificateTypeFromName(String templateName) {
        if (!StringUtils.hasText(templateName)) {
            return "";
        }
        String name = templateName.trim();
        if (name.endsWith("模板")) {
            return name.substring(0, name.length() - 2).trim();
        }
        return name;
    }

    private String extractExtField(String extJson, String fieldName) {
        if (!StringUtils.hasText(extJson) || !StringUtils.hasText(fieldName)) {
            return "";
        }
        try {
            var node = objectMapper.readTree(extJson);
            var value = node.get(fieldName);
            return value == null || value.isNull() ? "" : value.asText("");
        } catch (Exception e) {
            return "";
        }
    }

    private String normalizeOutputFormat(String outputFormat, String originalName) {
        String fmt = outputFormat == null ? "" : outputFormat.trim().toLowerCase(Locale.ROOT);
        if (fmt.equals("doc") || fmt.equals("docx")) {
            return "docx";
        }
        if (fmt.equals("pdf")) {
            return "pdf";
        }
        String name = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".doc") || name.endsWith(".docx")) {
            return "docx";
        }
        return "pdf";
    }
}

