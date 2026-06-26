package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.PartyReminderTaskFilterRequest;
import edu.ruc.platform.admin.dto.PartyReminderTaskResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.admin.service.MockAdminService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import edu.ruc.platform.party.domain.LatestPartyReminderTask;
import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import edu.ruc.platform.party.repository.LatestPartyFlowNodeRepository;
import edu.ruc.platform.party.repository.LatestPartyFlowRepository;
import edu.ruc.platform.party.repository.LatestPartyReminderTaskRepository;
import edu.ruc.platform.party.repository.LatestPartyStudentProgressRepository;
import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import edu.ruc.platform.party.domain.LatestPartyFlow;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/v1/admin/party-reminders")
@RequiredArgsConstructor
public class AdminPartyReminderController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;
    private final LatestPartyReminderTaskRepository latestPartyReminderTaskRepository;
    private final LatestPartyStudentProgressRepository latestPartyStudentProgressRepository;
    private final LatestPartyFlowNodeRepository latestPartyFlowNodeRepository;
    private final LatestPartyFlowRepository latestPartyFlowRepository;
    private final StudentProfileRepository studentProfileRepository;

    private static final Set<String> ALLOWED_CHANNELS = Set.of("miniprogram", "sms", "email");
    private static final Set<String> ALLOWED_STATUSES = Set.of("pending", "sent", "canceled", "failed");

    @GetMapping
    public ApiResponse<List<PartyReminderTaskResponse>> list(@RequestParam(required = false) String status,
                                                             @RequestParam(required = false) String channel,
                                                             @RequestParam(required = false) String studentKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listPartyReminderTasks(new PartyReminderTaskFilterRequest(status, channel, studentKeyword)));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<PartyReminderTaskResponse>> page(@RequestParam(required = false) String status,
                                                                     @RequestParam(required = false) String channel,
                                                                     @RequestParam(required = false) String studentKeyword,
                                                                     @Min(value = 0, message = "page不能小于0") @RequestParam(defaultValue = "0") int page,
                                                                     @Min(value = 1, message = "size不能小于1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pagePartyReminderTasks(new PartyReminderTaskFilterRequest(status, channel, studentKeyword), page, size));
    }

    @GetMapping("/meta/students")
    public ApiResponse<List<PartyStudentOption>> metaStudents(@RequestParam(required = false) String keyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        String kw = keyword == null ? null : keyword.trim();
        List<StudentProfile> list = (kw == null || kw.isBlank())
                ? studentProfileRepository.findAll(PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id"))).getContent()
                : studentProfileRepository.findTop50ByNameContainingIgnoreCaseOrStudentNoContainingIgnoreCaseOrderByIdDesc(kw, kw);
        return ApiResponse.success(list.stream()
                .map(p -> new PartyStudentOption(
                        p.getId(),
                        p.getStudentNo(),
                        p.getName(),
                        p.getGrade(),
                        p.getClassName()
                ))
                .toList());
    }

    @GetMapping("/meta/students/{studentUserId}/flows")
    public ApiResponse<List<PartyStudentFlowOption>> metaStudentFlows(@Positive(message = "studentUserId必须大于0") @PathVariable Long studentUserId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        List<LatestPartyStudentProgress> progresses = latestPartyStudentProgressRepository.findAllByStudentUserIdAndIsDeleted(studentUserId, 0);
        return ApiResponse.success(progresses.stream()
                .map(p -> {
                    LatestPartyFlow flow = latestPartyFlowRepository.findById(p.getFlowId()).orElse(null);
                    if (flow == null || flow.getIsDeleted() == null || flow.getIsDeleted() != 0) {
                        return null;
                    }
                    return new PartyStudentFlowOption(p.getId(), flow.getId(), flow.getFlowName());
                })
                .filter(Objects::nonNull)
                .toList());
    }

    @GetMapping("/meta/flows/{flowId}/nodes")
    public ApiResponse<List<PartyFlowNodeOption>> metaFlowNodes(@Positive(message = "flowId必须大于0") @PathVariable Long flowId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        List<LatestPartyFlowNode> nodes = latestPartyFlowNodeRepository.findByFlowIdAndIsDeletedOrderBySeqNoAsc(flowId, 0);
        return ApiResponse.success(nodes.stream()
                .map(n -> new PartyFlowNodeOption(n.getId(), n.getSeqNo(), n.getNodeName()))
                .toList());
    }

    @GetMapping("/meta/flows")
    public ApiResponse<List<PartyFlowOption>> metaFlows() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(latestPartyFlowRepository.findByIsDeletedOrderByIdAsc(0).stream()
                .map(f -> new PartyFlowOption(f.getId(), f.getFlowName()))
                .toList());
    }

    @PostMapping("/{id}/send")
    public ApiResponse<PartyReminderTaskResponse> send(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("提醒已发送", adminService.sendPartyReminder(id));
    }

    @PostMapping("/{id}/resend")
    public ApiResponse<PartyReminderTaskResponse> resend(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("提醒已重新发送", adminService.resendPartyReminder(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PartyReminderTaskResponse> cancel(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("提醒已取消", adminService.cancelPartyReminder(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (adminService instanceof MockAdminService mockAdminService) {
            mockAdminService.deletePartyReminder(id);
        } else {
            LatestPartyReminderTask task = latestPartyReminderTaskRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("提醒任务不存在"));
            latestPartyReminderTaskRepository.delete(task);
        }
        return ApiResponse.success("提醒任务已删除", null);
    }

    @PostMapping
    public ApiResponse<PartyReminderTaskResponse> create(@Valid @RequestBody CreatePartyReminderRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (adminService instanceof MockAdminService) {
            throw new BusinessException("当前环境暂不支持新增提醒任务");
        }
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        Long progressId = resolveProgressId(request.progressId(), request.studentNo(), request.flowId());
        Long nodeId = requirePositive(request.nodeId(), "节点ID必须大于0");
        LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new BusinessException("节点不存在"));
        LatestPartyStudentProgress progress = latestPartyStudentProgressRepository.findById(progressId)
                .orElseThrow(() -> new BusinessException("学生流程记录不存在"));
        if (!Objects.equals(node.getFlowId(), progress.getFlowId())) {
            throw new BusinessException("节点不属于该学生流程");
        }
        LocalDateTime dueAt = request.dueAt();
        if (dueAt == null) {
            throw new BusinessException("计划时间不能为空");
        }
        String channel = normalizeEnumValue(request.channel(), "miniprogram", ALLOWED_CHANNELS, "发送渠道不合法");
        String status = normalizeEnumValue(request.status(), "pending", ALLOWED_STATUSES, "状态不合法");

        LatestPartyReminderTask task = new LatestPartyReminderTask();
        task.setProgressId(progressId);
        task.setNodeId(nodeId);
        task.setDueAt(dueAt);
        task.setChannel(channel);
        task.setStatus(status);
        task.setSentAt("sent".equals(status) ? LocalDateTime.now() : null);
        task = latestPartyReminderTaskRepository.save(task);
        return ApiResponse.success("提醒任务已创建", toResponse(task));
    }

    @PutMapping("/{id}")
    public ApiResponse<PartyReminderTaskResponse> update(@Positive(message = "提醒任务ID必须大于0") @PathVariable Long id,
                                                         @Valid @RequestBody UpdatePartyReminderRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        if (adminService instanceof MockAdminService mockAdminService) {
            if (request.dueAt() == null) {
                throw new BusinessException("计划时间不能为空");
            }
            return ApiResponse.success("已更新计划时间", mockAdminService.updatePartyReminderDueAt(id, request.dueAt()));
        }
        LatestPartyReminderTask task = latestPartyReminderTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("提醒任务不存在"));
        boolean changed = false;

        if (request.dueAt() != null) {
            task.setDueAt(request.dueAt());
            changed = true;
        }
        if (request.channel() != null && !request.channel().isBlank()) {
            task.setChannel(normalizeEnumValue(request.channel(), task.getChannel(), ALLOWED_CHANNELS, "发送渠道不合法"));
            changed = true;
        }
        if (request.status() != null && !request.status().isBlank()) {
            String nextStatus = normalizeEnumValue(request.status(), task.getStatus(), ALLOWED_STATUSES, "状态不合法");
            task.setStatus(nextStatus);
            if ("sent".equalsIgnoreCase(nextStatus) && task.getSentAt() == null) {
                task.setSentAt(LocalDateTime.now());
            }
            changed = true;
        }
        if (request.nodeId() != null) {
            Long nextNodeId = requirePositive(request.nodeId(), "节点ID必须大于0");
            LatestPartyStudentProgress progress = latestPartyStudentProgressRepository.findById(task.getProgressId())
                    .orElseThrow(() -> new BusinessException("学生流程记录不存在"));
            LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(nextNodeId)
                    .orElseThrow(() -> new BusinessException("节点不存在"));
            if (!Objects.equals(node.getFlowId(), progress.getFlowId())) {
                throw new BusinessException("节点不属于该学生流程");
            }
            task.setNodeId(nextNodeId);
            changed = true;
        }
        if (!changed) {
            throw new BusinessException("未提供可更新字段");
        }
        task = latestPartyReminderTaskRepository.save(task);
        return ApiResponse.success("提醒任务已更新", toResponse(task));
    }

    @PostMapping("/import")
    public ApiResponse<BatchImportResultResponse> importExcel(@RequestParam("file") MultipartFile file,
                                                              @RequestParam(required = false) String defaultChannel,
                                                              @RequestParam(required = false) String defaultStatus) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        if (adminService instanceof MockAdminService) {
            throw new BusinessException("当前环境暂不支持导入提醒任务");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String fallbackChannel = normalizeEnumValue(defaultChannel, "miniprogram", ALLOWED_CHANNELS, "默认渠道不合法");
        String fallbackStatus = normalizeEnumValue(defaultStatus, "pending", ALLOWED_STATUSES, "默认状态不合法");

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
                    LocalDateTime dueAt = getCellDateTimeValue(row, 3);
                    String channel = getCellStringValue(row, 4);
                    String status = getCellStringValue(row, 5);

                    if (studentNo == null || studentNo.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "studentNo", "学号不能为空", ""));
                        continue;
                    }
                    if (flowId == null || flowId <= 0) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "flowId", "流程ID必须大于0", String.valueOf(flowId)));
                        continue;
                    }
                    if (nodeId == null || nodeId <= 0) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "nodeId", "节点ID必须大于0", String.valueOf(nodeId)));
                        continue;
                    }
                    if (dueAt == null) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(rowNumber, "dueAt", "计划时间不能为空", ""));
                        continue;
                    }

                    Long progressId = resolveProgressId(null, studentNo, flowId);
                    LatestPartyStudentProgress progress = latestPartyStudentProgressRepository.findById(progressId)
                            .orElseThrow(() -> new BusinessException("学生流程记录不存在"));
                    LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(nodeId)
                            .orElseThrow(() -> new BusinessException("节点不存在"));
                    if (!Objects.equals(node.getFlowId(), progress.getFlowId())) {
                        throw new BusinessException("节点不属于该学生流程");
                    }

                    String finalChannel = channel == null || channel.isBlank()
                            ? fallbackChannel
                            : normalizeEnumValue(channel, fallbackChannel, ALLOWED_CHANNELS, "发送渠道不合法");
                    String finalStatus = status == null || status.isBlank()
                            ? fallbackStatus
                            : normalizeEnumValue(status, fallbackStatus, ALLOWED_STATUSES, "状态不合法");

                    LatestPartyReminderTask task = new LatestPartyReminderTask();
                    task.setProgressId(progressId);
                    task.setNodeId(nodeId);
                    task.setDueAt(dueAt);
                    task.setChannel(finalChannel);
                    task.setStatus(finalStatus);
                    task.setSentAt("sent".equals(finalStatus) ? LocalDateTime.now() : null);
                    latestPartyReminderTaskRepository.save(task);
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
                            @RequestParam(required = false) String channel,
                            @RequestParam(required = false) String studentKeyword,
                            HttpServletResponse response) throws IOException {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        List<PartyReminderTaskResponse> items = adminService.listPartyReminderTasks(new PartyReminderTaskFilterRequest(status, channel, studentKeyword));
        byte[] data = exportItems(items);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=party-reminders.xlsx");
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    private byte[] exportItems(List<PartyReminderTaskResponse> items) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("提醒任务");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("任务ID");
            headerRow.createCell(1).setCellValue("学生ID");
            headerRow.createCell(2).setCellValue("学号");
            headerRow.createCell(3).setCellValue("姓名");
            headerRow.createCell(4).setCellValue("节点ID");
            headerRow.createCell(5).setCellValue("节点名称");
            headerRow.createCell(6).setCellValue("计划时间");
            headerRow.createCell(7).setCellValue("渠道");
            headerRow.createCell(8).setCellValue("状态");
            headerRow.createCell(9).setCellValue("发送时间");
            headerRow.createCell(10).setCellValue("失败原因");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (PartyReminderTaskResponse it : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(it.id() == null ? 0 : it.id());
                row.createCell(1).setCellValue(it.studentUserId() == null ? 0 : it.studentUserId());
                row.createCell(2).setCellValue(it.studentNo() == null ? "" : it.studentNo());
                row.createCell(3).setCellValue(it.studentName() == null ? "" : it.studentName());
                row.createCell(4).setCellValue(it.nodeId() == null ? 0 : it.nodeId());
                row.createCell(5).setCellValue(it.nodeName() == null ? "" : it.nodeName());
                row.createCell(6).setCellValue(it.dueAt() == null ? "" : dtf.format(it.dueAt()));
                row.createCell(7).setCellValue(it.channel() == null ? "" : it.channel());
                row.createCell(8).setCellValue(it.status() == null ? "" : it.status());
                row.createCell(9).setCellValue(it.sentAt() == null ? "" : dtf.format(it.sentAt()));
                row.createCell(10).setCellValue(it.errorMessage() == null ? "" : it.errorMessage());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    private PartyReminderTaskResponse toResponse(LatestPartyReminderTask task) {
        LatestPartyStudentProgress progress = latestPartyStudentProgressRepository.findById(task.getProgressId()).orElse(null);
        Long studentUserId = progress == null ? null : progress.getStudentUserId();
        Long flowId = progress == null ? null : progress.getFlowId();
        String flowName = flowId == null ? null : latestPartyFlowRepository.findById(flowId).map(LatestPartyFlow::getFlowName).orElse(null);
        String studentName = studentUserId == null
                ? null
                : studentProfileRepository.findById(studentUserId).map(p -> p.getName()).orElse(null);
        String studentNo = studentUserId == null
                ? null
                : studentProfileRepository.findById(studentUserId).map(p -> p.getStudentNo()).orElse(null);
        LatestPartyFlowNode node = latestPartyFlowNodeRepository.findById(task.getNodeId()).orElse(null);
        return new PartyReminderTaskResponse(
                task.getId(),
                task.getProgressId(),
                flowId,
                flowName,
                task.getNodeId(),
                node == null ? null : node.getNodeName(),
                studentUserId,
                studentName,
                studentNo,
                task.getDueAt(),
                task.getChannel(),
                task.getStatus(),
                task.getSentAt(),
                null,
                task.getCreatedAt()
        );
    }

    private Long resolveProgressId(Long progressId, String studentNo, Long flowId) {
        if (progressId != null && progressId > 0) {
            return progressId;
        }
        String no = studentNo == null ? null : studentNo.trim();
        if (no == null || no.isBlank()) {
            throw new BusinessException("progressId 与 studentNo 至少需要提供一个");
        }
        if (flowId == null || flowId <= 0) {
            throw new BusinessException("flowId必须大于0");
        }
        Long studentUserId = studentProfileRepository.findByStudentNo(no)
                .map(p -> p.getId())
                .orElseThrow(() -> new BusinessException("学生不存在: " + no));
        return latestPartyStudentProgressRepository.findByStudentUserIdAndFlowIdAndIsDeleted(studentUserId, flowId, 0)
                .map(LatestPartyStudentProgress::getId)
                .orElseThrow(() -> new BusinessException("未找到该学生的流程记录"));
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw new BusinessException(message);
        }
        return value;
    }

    private String normalizeEnumValue(String raw, String fallback, Set<String> allowed, String message) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if (!allowed.contains(v)) {
            throw new BusinessException(message + ": " + raw);
        }
        return v;
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
                return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
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

    public record CreatePartyReminderRequest(
            Long progressId,
            String studentNo,
            Long flowId,
            Long nodeId,
            LocalDateTime dueAt,
            String channel,
            String status
    ) {
    }

    public record UpdatePartyReminderRequest(
            Long nodeId,
            LocalDateTime dueAt,
            String channel,
            String status
    ) {
    }

    public record PartyStudentOption(
            Long studentUserId,
            String studentNo,
            String studentName,
            String grade,
            String className
    ) {
    }

    public record PartyStudentFlowOption(Long progressId, Long flowId, String flowName) {
    }

    public record PartyFlowNodeOption(Long nodeId, Integer seqNo, String nodeName) {
    }

    public record PartyFlowOption(Long flowId, String flowName) {
    }
}
