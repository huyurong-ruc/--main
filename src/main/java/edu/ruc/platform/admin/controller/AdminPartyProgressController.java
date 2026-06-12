package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.LatestPartyFlow;
import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyFlowRepository;
import edu.ruc.platform.party.repository.LatestPartyStudentProgressRepository;
import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@Validated
@RequestMapping("/api/v1/admin/party-progress")
@RequiredArgsConstructor
public class AdminPartyProgressController {

    private final CurrentUserService currentUserService;
    private final LatestPartyStudentProgressRepository latestPartyStudentProgressRepository;
    private final LatestPartyFlowRepository latestPartyFlowRepository;
    private final LatestPartyFlowNodeRepository latestPartyFlowNodeRepository;
    private final StudentProfileRepository studentProfileRepository;

    @GetMapping("/page")
    public ApiResponse<PageResponse<PartyStudentProgressResponse>> page(@RequestParam(required = false) String status,
                                                                        @RequestParam(required = false) String studentKeyword,
                                                                        @RequestParam(required = false) String grade,
                                                                        @RequestParam(required = false) String className,
                                                                        @RequestParam(required = false) Long flowId,
                                                                        @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                        @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        List<PartyStudentProgressResponse> rows = query(status, studentKeyword, grade, className, flowId);

        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<PartyStudentProgressResponse> content = rows.subList(from, to);
        int totalPages = (int) Math.ceil(rows.size() / (double) size);
        return ApiResponse.success(new PageResponse<>(content, rows.size(), totalPages, page, size));
    }

    @PostMapping
    public ApiResponse<PartyStudentProgressResponse> create(@Valid @RequestBody UpsertPartyStudentProgressRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        StudentProfile student = requireStudentByNo(request.studentNo());
        Long flowId = requirePositive(request.flowId(), "流程ID必须大于0");
        latestPartyFlowRepository.findById(flowId)
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("流程不存在"));
        if (latestPartyStudentProgressRepository.findByStudentUserIdAndFlowIdAndIsDeleted(student.getId(), flowId, 0).isPresent()) {
            throw new BusinessException("该学生该流程已存在记录");
        }
        Long nodeId = request.currentNodeId();
        if (nodeId != null) {
            requireNodeBelongsToFlow(nodeId, flowId);
        }
        String status = normalizeProgressStatus(request.status());

        LatestPartyStudentProgress entity = new LatestPartyStudentProgress();
        entity.setStudentUserId(student.getId());
        entity.setFlowId(flowId);
        entity.setCurrentNodeId(nodeId);
        entity.setStatus(status);
        entity.setStartedAt(request.startedAt());
        entity.setUpdatedNodeAt(request.updatedNodeAt());
        entity.setNextDeadlineAt(request.nextDeadlineAt());
        entity.setIsDeleted(0);
        entity = latestPartyStudentProgressRepository.save(entity);
        return ApiResponse.success("学生流程记录已创建", toResponse(entity, null));
    }

    @PutMapping("/{id}")
    public ApiResponse<PartyStudentProgressResponse> update(@Positive(message = "记录ID必须大于0") @PathVariable Long id,
                                                            @Valid @RequestBody UpsertPartyStudentProgressRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        LatestPartyStudentProgress entity = latestPartyStudentProgressRepository.findById(id)
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("记录不存在"));

        StudentProfile student = requireStudentByNo(request.studentNo());
        Long flowId = requirePositive(request.flowId(), "流程ID必须大于0");
        latestPartyFlowRepository.findById(flowId)
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException("流程不存在"));
        Long nodeId = request.currentNodeId();
        if (nodeId != null) {
            requireNodeBelongsToFlow(nodeId, flowId);
        }
        String status = normalizeProgressStatus(request.status());

        entity.setStudentUserId(student.getId());
        entity.setFlowId(flowId);
        entity.setCurrentNodeId(nodeId);
        entity.setStatus(status);
        entity.setStartedAt(request.startedAt());
        entity.setUpdatedNodeAt(request.updatedNodeAt());
        entity.setNextDeadlineAt(request.nextDeadlineAt());
        entity = latestPartyStudentProgressRepository.save(entity);
        return ApiResponse.success("学生流程记录已更新", toResponse(entity, null));
    }

    @PostMapping("/import")
    public ApiResponse<BatchImportResultResponse> importExcel(@RequestParam("file") MultipartFile file) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                totalRows += 1;
                int rowNumber = i + 1;
                try {
                    String studentNo = getCellStringValue(row, 0);
                    Long flowId = getCellLongValue(row, 1);
                    Long nodeId = getCellLongValue(row, 2);
                    String status = getCellStringValue(row, 3);
                    LocalDateTime startedAt = getCellDateTimeValue(row, 4);
                    LocalDateTime updatedNodeAt = getCellDateTimeValue(row, 5);
                    LocalDateTime nextDeadlineAt = getCellDateTimeValue(row, 6);

                    if (studentNo == null || studentNo.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "studentNo", "学号不能为空", ""));
                        continue;
                    }
                    if (flowId == null || flowId <= 0) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "flowId", "流程ID必须大于0", String.valueOf(flowId)));
                        continue;
                    }
                    if (nodeId != null && nodeId <= 0) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "currentNodeId", "节点ID必须大于0", String.valueOf(nodeId)));
                        continue;
                    }

                    StudentProfile student = requireStudentByNo(studentNo);
                    latestPartyFlowRepository.findById(flowId)
                            .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                            .orElseThrow(() -> new BusinessException("流程不存在"));
                    if (nodeId != null) {
                        requireNodeBelongsToFlow(nodeId, flowId);
                    }
                    String normalizedStatus = normalizeProgressStatus(status);

                    LatestPartyStudentProgress entity = latestPartyStudentProgressRepository
                            .findByStudentUserIdAndFlowIdAndIsDeleted(student.getId(), flowId, 0)
                            .orElse(null);
                    if (entity == null) {
                        entity = new LatestPartyStudentProgress();
                        entity.setStudentUserId(student.getId());
                        entity.setFlowId(flowId);
                        entity.setIsDeleted(0);
                    }
                    entity.setCurrentNodeId(nodeId);
                    entity.setStatus(normalizedStatus);
                    entity.setStartedAt(startedAt);
                    entity.setUpdatedNodeAt(updatedNodeAt);
                    entity.setNextDeadlineAt(nextDeadlineAt);
                    latestPartyStudentProgressRepository.save(entity);
                    successRows += 1;
                } catch (Exception ex) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "general", ex.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        return ApiResponse.success("导入完成", new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors));
    }

    @GetMapping("/export")
    public void exportExcel(@RequestParam(required = false) String status,
                            @RequestParam(required = false) String studentKeyword,
                            @RequestParam(required = false) String grade,
                            @RequestParam(required = false) String className,
                            @RequestParam(required = false) Long flowId,
                            HttpServletResponse response) throws IOException {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        byte[] data = exportRows(query(status, studentKeyword, grade, className, flowId));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=party-progress.xlsx");
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    private List<PartyStudentProgressResponse> query(String status,
                                                     String studentKeyword,
                                                     String grade,
                                                     String className,
                                                     Long flowId) {
        String kw = normalizeText(studentKeyword);
        String normalizedStatus = normalizeText(status);
        String normalizedGrade = normalizeText(grade);
        String normalizedClassName = normalizeText(className);

        Map<Long, LatestPartyFlow> flows = new HashMap<>();
        for (LatestPartyFlow flow : latestPartyFlowRepository.findByIsDeletedOrderByIdAsc(0)) {
            flows.put(flow.getId(), flow);
        }

        List<LatestPartyStudentProgress> all = latestPartyStudentProgressRepository.findAll().stream()
                .filter(item -> item != null && item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .filter(item -> flowId == null || Objects.equals(item.getFlowId(), flowId))
                .filter(item -> normalizedStatus == null || normalizedStatus.equalsIgnoreCase(item.getStatus()))
                .toList();

        return all.stream()
                .map(item -> toResponse(item, flows))
                .filter(item -> matchesStudentKeyword(item, kw))
                .filter(item -> normalizedGrade == null || (item.studentGrade() != null && normalizedGrade.equals(item.studentGrade())))
                .filter(item -> normalizedClassName == null || (item.studentClassName() != null && normalizedClassName.equals(item.studentClassName())))
                .sorted(Comparator.comparing(PartyStudentProgressResponse::updatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(PartyStudentProgressResponse::id, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private byte[] exportRows(List<PartyStudentProgressResponse> items) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生进度");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("记录ID");
            headerRow.createCell(1).setCellValue("学生ID");
            headerRow.createCell(2).setCellValue("学号");
            headerRow.createCell(3).setCellValue("姓名");
            headerRow.createCell(4).setCellValue("年级");
            headerRow.createCell(5).setCellValue("班级");
            headerRow.createCell(6).setCellValue("流程ID");
            headerRow.createCell(7).setCellValue("流程名称");
            headerRow.createCell(8).setCellValue("当前节点ID");
            headerRow.createCell(9).setCellValue("当前节点");
            headerRow.createCell(10).setCellValue("状态");
            headerRow.createCell(11).setCellValue("开始时间");
            headerRow.createCell(12).setCellValue("节点更新时间");
            headerRow.createCell(13).setCellValue("下次截止时间");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (PartyStudentProgressResponse it : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(it.id() == null ? 0 : it.id());
                row.createCell(1).setCellValue(it.studentUserId() == null ? 0 : it.studentUserId());
                row.createCell(2).setCellValue(it.studentNo() == null ? "" : it.studentNo());
                row.createCell(3).setCellValue(it.studentName() == null ? "" : it.studentName());
                row.createCell(4).setCellValue(it.studentGrade() == null ? "" : it.studentGrade());
                row.createCell(5).setCellValue(it.studentClassName() == null ? "" : it.studentClassName());
                row.createCell(6).setCellValue(it.flowId() == null ? 0 : it.flowId());
                row.createCell(7).setCellValue(it.flowName() == null ? "" : it.flowName());
                row.createCell(8).setCellValue(it.currentNodeId() == null ? 0 : it.currentNodeId());
                row.createCell(9).setCellValue(it.currentNodeName() == null ? "" : it.currentNodeName());
                row.createCell(10).setCellValue(it.status() == null ? "" : it.status());
                row.createCell(11).setCellValue(it.startedAt() == null ? "" : dtf.format(it.startedAt()));
                row.createCell(12).setCellValue(it.updatedNodeAt() == null ? "" : dtf.format(it.updatedNodeAt()));
                row.createCell(13).setCellValue(it.nextDeadlineAt() == null ? "" : dtf.format(it.nextDeadlineAt()));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    private PartyStudentProgressResponse toResponse(LatestPartyStudentProgress entity, Map<Long, LatestPartyFlow> flows) {
        StudentProfile profile = entity.getStudentUserId() == null ? null : studentProfileRepository.findById(entity.getStudentUserId()).orElse(null);
        LatestPartyFlow flow = flows != null ? flows.get(entity.getFlowId()) : entity.getFlowId() == null ? null : latestPartyFlowRepository.findById(entity.getFlowId()).orElse(null);
        LatestPartyFlowNode node = entity.getCurrentNodeId() == null ? null : latestPartyFlowNodeRepository.findById(entity.getCurrentNodeId()).orElse(null);
        return new PartyStudentProgressResponse(
                entity.getId(),
                entity.getStudentUserId(),
                profile == null ? null : profile.getStudentNo(),
                profile == null ? null : profile.getName(),
                profile == null ? null : profile.getGrade(),
                profile == null ? null : profile.getClassName(),
                entity.getFlowId(),
                flow == null ? null : flow.getFlowName(),
                entity.getCurrentNodeId(),
                node == null ? null : node.getNodeName(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getUpdatedNodeAt(),
                entity.getNextDeadlineAt(),
                entity.getUpdatedAt()
        );
    }

    private boolean matchesStudentKeyword(PartyStudentProgressResponse item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String kw = keyword.trim();
        return (item.studentName() != null && item.studentName().contains(kw))
                || (item.studentNo() != null && item.studentNo().contains(kw));
    }

    private StudentProfile requireStudentByNo(String studentNo) {
        String no = normalizeText(studentNo);
        if (no == null) {
            throw new BusinessException("学号不能为空");
        }
        return studentProfileRepository.findByStudentNo(no)
                .orElseThrow(() -> new BusinessException("学生不存在: " + no));
    }

    private void requireNodeBelongsToFlow(Long nodeId, Long flowId) {
        LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new BusinessException("节点不存在"));
        if (!Objects.equals(node.getFlowId(), flowId)) {
            throw new BusinessException("节点不属于该流程");
        }
        if (node.getIsDeleted() != null && node.getIsDeleted() != 0) {
            throw new BusinessException("节点已被删除");
        }
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new BusinessException(message);
        }
        return value;
    }

    private String normalizeText(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        return v.isBlank() ? null : v;
    }

    private String normalizeProgressStatus(String raw) {
        String v = normalizeText(raw);
        if (v == null) {
            return "in_progress";
        }
        String s = v.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "not_started", "in_progress", "paused", "completed" -> s;
            default -> throw new BusinessException("状态不合法: " + raw);
        };
    }

    private String getCellStringValue(Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> null;
            };
        } catch (Exception ex) {
            return null;
        }
    }

    private Long getCellLongValue(Row row, int idx) {
        String raw = getCellStringValue(row, idx);
        if (raw == null || raw.isBlank()) return null;
        try {
            return Long.valueOf(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime getCellDateTimeValue(Row row, int idx) {
        var cell = row.getCell(idx);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                java.util.Date date = cell.getDateCellValue();
                if (date == null) return null;
                return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            }
            String raw = getCellStringValue(row, idx);
            if (raw == null || raw.isBlank()) return null;
            String text = raw.trim().replace(" ", "T");
            if (text.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                text = text + ":00";
            }
            return LocalDateTime.parse(text);
        } catch (Exception ex) {
            return null;
        }
    }

    public record UpsertPartyStudentProgressRequest(
            String studentNo,
            Long flowId,
            Long currentNodeId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime updatedNodeAt,
            LocalDateTime nextDeadlineAt
    ) {
    }

    public record PartyStudentProgressResponse(
            Long id,
            Long studentUserId,
            String studentNo,
            String studentName,
            String studentGrade,
            String studentClassName,
            Long flowId,
            String flowName,
            Long currentNodeId,
            String currentNodeName,
            String status,
            LocalDateTime startedAt,
            LocalDateTime updatedNodeAt,
            LocalDateTime nextDeadlineAt,
            LocalDateTime updatedAt
    ) {
    }
}
