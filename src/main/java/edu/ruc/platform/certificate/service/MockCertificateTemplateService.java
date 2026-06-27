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

    private final AtomicLong idGenerator = new AtomicLong(100);
    private final Map<Long, CertificateTemplateResponse> templatesById = new ConcurrentHashMap<>();
    private final Map<String, Long> idByCode = new ConcurrentHashMap<>();

    public MockCertificateTemplateService() {
        initializeDefaultTemplates();
    }

    private void initializeDefaultTemplates() {
        createTemplate(1L, "CERT_001", "在读证明模板", "在读证明",
                "兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，当前学籍状态为在读。",
                "/templates/cert/study-certificate.pdf", "PDF", "用于学生在读状态证明");
        createTemplate(2L, "CERT_002", "党员身份证明模板", "党员身份证明",
                "兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业学生，该生于{{joinDate}}加入中国共产党，当前党组织关系在我院。",
                "/templates/cert/party-member-certificate.pdf", "PDF", "用于党员身份证明");
        createTemplate(3L, "CERT_003", "困难认定证明模板", "困难认定证明",
                "兹证明{{studentName}}同学（学号：{{studentNo}}）系我院{{majorName}}专业{{gradeYear}}级学生，经学院认定，该生家庭经济困难等级为{{difficultyLevel}}。",
                "/templates/cert/difficulty-certificate.pdf", "PDF", "用于学生困难认定证明");
        createTemplate(4L, "CERT_004", "成绩单模板", "成绩单",
                "兹证明{{studentName}}同学（学号：{{studentNo}}）在我院{{majorName}}专业学习期间，各科成绩如下：{{grades}}",
                "/templates/cert/transcript.pdf", "PDF", "用于学生成绩证明");
        createTemplate(5L, "CERT_005", "实习证明模板", "实习证明",
                "兹证明{{studentName}}同学（学号：{{studentNo}}）于{{startDate}}至{{endDate}}在{{companyName}}实习，实习岗位为{{position}}。",
                "/templates/cert/internship-certificate.pdf", "PDF", "用于学生实习证明");
    }

    private void createTemplate(Long id, String code, String name, String type, String content, String filePath, String format, String desc) {
        LocalDateTime now = LocalDateTime.now();
        CertificateTemplateResponse template = new CertificateTemplateResponse(
                id, code, name, type, content, filePath, format, true, desc, null, now, now
        );
        templatesById.put(id, template);
        idByCode.put(code, id);
    }

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
