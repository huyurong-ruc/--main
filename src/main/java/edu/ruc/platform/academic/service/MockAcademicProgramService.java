package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.dto.AcademicProgramCreateRequest;
import edu.ruc.platform.academic.dto.AcademicProgramModuleCreateRequest;
import edu.ruc.platform.academic.dto.AcademicProgramModuleResponse;
import edu.ruc.platform.academic.dto.AcademicProgramResponse;
import edu.ruc.platform.academic.dto.AuditReportResponse;
import edu.ruc.platform.academic.dto.TranscriptUploadResponse;
import edu.ruc.platform.common.exception.BusinessException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockAcademicProgramService implements AcademicProgramApplicationService {

    private final AtomicLong programIdGenerator = new AtomicLong(1);
    private final AtomicLong moduleIdGenerator = new AtomicLong(1);
    private final AtomicLong transcriptIdGenerator = new AtomicLong(1);

    private final Map<Long, AcademicProgramResponse> programs = new ConcurrentHashMap<>();
    private final Map<Long, List<AcademicProgramModuleResponse>> programModules = new ConcurrentHashMap<>();
    private final Map<Long, TranscriptUploadResponse> latestTranscriptByStudent = new ConcurrentHashMap<>();

    @Override
    public List<AcademicProgramResponse> listAll() {
        return programs.values().stream()
                .sorted(Comparator.comparing(AcademicProgramResponse::id))
                .toList();
    }

    @Override
    public AcademicProgramResponse getById(Long id) {
        AcademicProgramResponse program = programs.get(id);
        if (program == null) {
            throw new BusinessException("培养方案不存在");
        }
        return program;
    }

    @Override
    public AcademicProgramResponse create(AcademicProgramCreateRequest request) {
        Long id = programIdGenerator.incrementAndGet();
        AcademicProgramResponse created = new AcademicProgramResponse(
                id,
                request.programCode(),
                request.programName(),
                request.major(),
                request.grade(),
                request.totalCredits(),
                request.description(),
                true,
                List.of(),
                LocalDateTime.now()
        );
        programs.put(id, created);
        programModules.put(id, new ArrayList<>());
        return created;
    }

    @Override
    public AcademicProgramResponse update(Long id, AcademicProgramCreateRequest request) {
        AcademicProgramResponse existing = programs.get(id);
        if (existing == null) {
            throw new BusinessException("培养方案不存在");
        }
        List<AcademicProgramModuleResponse> modules = programModules.getOrDefault(id, List.of());
        AcademicProgramResponse updated = new AcademicProgramResponse(
                existing.id(),
                existing.programCode(),
                request.programName(),
                request.major(),
                request.grade(),
                request.totalCredits(),
                request.description(),
                existing.isActive(),
                modules,
                existing.createdAt()
        );
        programs.put(id, updated);
        return updated;
    }

    @Override
    public void delete(Long id) {
        if (!programs.containsKey(id)) {
            throw new BusinessException("培养方案不存在");
        }
        programs.remove(id);
        programModules.remove(id);
    }

    @Override
    public AcademicProgramModuleResponse addModule(Long programId, AcademicProgramModuleCreateRequest request) {
        AcademicProgramResponse existing = programs.get(programId);
        if (existing == null) {
            throw new BusinessException("培养方案不存在");
        }
        Long moduleId = moduleIdGenerator.incrementAndGet();
        AcademicProgramModuleResponse module = new AcademicProgramModuleResponse(
                moduleId,
                request.moduleCode(),
                request.moduleName(),
                request.moduleType(),
                request.requiredCredits(),
                request.description(),
                request.sortOrder() != null ? request.sortOrder() : 0
        );
        programModules.computeIfAbsent(programId, key -> new ArrayList<>()).add(module);
        List<AcademicProgramModuleResponse> modules = programModules.getOrDefault(programId, List.of());
        programs.put(programId, new AcademicProgramResponse(
                existing.id(),
                existing.programCode(),
                existing.programName(),
                existing.major(),
                existing.grade(),
                existing.totalCredits(),
                existing.description(),
                existing.isActive(),
                modules.stream().sorted(Comparator.comparingInt(AcademicProgramModuleResponse::sortOrder)).toList(),
                existing.createdAt()
        ));
        return module;
    }

    @Override
    public void removeModule(Long moduleId) {
        boolean removed = false;
        for (Map.Entry<Long, List<AcademicProgramModuleResponse>> entry : programModules.entrySet()) {
            removed = entry.getValue().removeIf(m -> m.id().equals(moduleId)) || removed;
        }
        if (!removed) {
            throw new BusinessException("模块不存在");
        }
    }

    @Override
    public TranscriptUploadResponse uploadTranscript(Long studentId, MultipartFile file) {
        Long transcriptId = transcriptIdGenerator.incrementAndGet();
        TranscriptUploadResponse response = new TranscriptUploadResponse(
                transcriptId,
                studentId,
                "MOCK",
                0.0,
                0.0,
                0,
                0,
                0,
                List.of(),
                LocalDateTime.now()
        );
        latestTranscriptByStudent.put(studentId, response);
        return response;
    }

    @Override
    public TranscriptUploadResponse getTranscript(Long studentId) {
        TranscriptUploadResponse response = latestTranscriptByStudent.get(studentId);
        if (response == null) {
            throw new BusinessException("未找到成绩单");
        }
        return response;
    }

    @Override
    public AuditReportResponse generateAuditReport(Long studentId, Long programId) {
        AcademicProgramResponse program = programs.get(programId);
        if (program == null) {
            throw new BusinessException("培养方案不存在");
        }
        return new AuditReportResponse(
                studentId,
                "",
                program.programName(),
                program.totalCredits(),
                0,
                program.totalCredits(),
                0.0,
                List.of(),
                List.of()
        );
    }
}
