package edu.ruc.platform.certificate.service;

import edu.ruc.platform.certificate.dto.CertificateTemplateCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateTemplateResponse;
import edu.ruc.platform.certificate.dto.CertificateTemplateUpdateRequest;
import edu.ruc.platform.common.exception.BusinessException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockCertificateTemplateService implements CertificateTemplateApplicationService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, CertificateTemplateResponse> templatesById = new ConcurrentHashMap<>();
    private final Map<String, Long> idByCode = new ConcurrentHashMap<>();

    @Override
    public List<CertificateTemplateResponse> listAll() {
        return templatesById.values().stream()
                .sorted(Comparator.comparing(CertificateTemplateResponse::id))
                .toList();
    }

    @Override
    public List<CertificateTemplateResponse> listActive() {
        return templatesById.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.isActive()))
                .sorted(Comparator.comparing(CertificateTemplateResponse::id))
                .toList();
    }

    @Override
    public List<CertificateTemplateResponse> listByType(String certificateType) {
        return templatesById.values().stream()
                .filter(item -> certificateType != null && certificateType.equals(item.certificateType()))
                .sorted(Comparator.comparing(CertificateTemplateResponse::id))
                .toList();
    }

    @Override
    public CertificateTemplateResponse getById(Long id) {
        CertificateTemplateResponse item = templatesById.get(id);
        if (item == null) {
            throw new BusinessException("模板不存在");
        }
        return item;
    }

    @Override
    public CertificateTemplateResponse getByCode(String templateCode) {
        Long id = idByCode.get(templateCode);
        if (id == null) {
            throw new BusinessException("模板不存在");
        }
        return getById(id);
    }

    @Override
    public CertificateTemplateResponse create(CertificateTemplateCreateRequest request) {
        if (idByCode.containsKey(request.templateCode())) {
            throw new BusinessException("模板编码已存在: " + request.templateCode());
        }
        Long id = idGenerator.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        CertificateTemplateResponse created = new CertificateTemplateResponse(
                id,
                request.templateCode(),
                request.templateName(),
                request.certificateType(),
                request.templateContent(),
                request.templateFilePath(),
                request.outputFormat(),
                true,
                request.description(),
                null,
                now,
                now
        );
        templatesById.put(id, created);
        idByCode.put(request.templateCode(), id);
        return created;
    }

    @Override
    public CertificateTemplateResponse update(Long id, CertificateTemplateUpdateRequest request) {
        CertificateTemplateResponse existing = templatesById.get(id);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        CertificateTemplateResponse updated = new CertificateTemplateResponse(
                existing.id(),
                existing.templateCode(),
                request.templateName() != null ? request.templateName() : existing.templateName(),
                request.certificateType() != null ? request.certificateType() : existing.certificateType(),
                request.templateContent() != null ? request.templateContent() : existing.templateContent(),
                request.templateFilePath() != null ? request.templateFilePath() : existing.templateFilePath(),
                request.outputFormat() != null ? request.outputFormat() : existing.outputFormat(),
                request.isActive() != null ? request.isActive() : existing.isActive(),
                request.description() != null ? request.description() : existing.description(),
                existing.updatedBy(),
                existing.createdAt(),
                now
        );
        templatesById.put(id, updated);
        return updated;
    }

    @Override
    public void delete(Long id) {
        CertificateTemplateResponse existing = templatesById.remove(id);
        if (existing == null) {
            throw new BusinessException("模板不存在");
        }
        idByCode.remove(existing.templateCode());
    }

    @Override
    public String renderPreview(Long templateId, Long studentId) {
        CertificateTemplateResponse item = getById(templateId);
        String content = item.templateContent();
        return content == null ? "" : content;
    }
}
