package edu.ruc.platform.platform.service;

import edu.ruc.platform.auth.domain.UserAccount;
import edu.ruc.platform.auth.repository.UserAccountRepository;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import edu.ruc.platform.student.domain.StudentAwardSupportRecord;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.repository.StudentAwardSupportRecordRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class ExcelImportExportServiceImpl implements ExcelImportExportService {

    private final UserAccountRepository userAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentAwardSupportRecordRepository studentAwardSupportRecordRepository;
    private final StudentProfileApplicationService studentProfileService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public BatchImportResultResponse importUsers(MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                totalRows++;
                try {
                    String username = getCellStringValue(row, 0);
                    String name = getCellStringValue(row, 1);
                    String role = getCellStringValue(row, 2);
                    String rawPassword = getCellStringValue(row, 3);
                    
                    if (username == null || username.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "username", "用户名不能为空", ""));
                        continue;
                    }
                    
                    if (userAccountRepository.findByUsername(username).isPresent()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "username", "用户名已存在: " + username, username));
                        continue;
                    }
                    
                    RoleType roleType;
                    try {
                        roleType = resolveRoleType(role);
                    } catch (Exception e) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "role", "角色不存在: " + role, role));
                        continue;
                    }
                    
                    UserAccount account = new UserAccount();
                    account.setUsername(username);
                    account.setPasswordHash(passwordEncoder.encode(rawPassword != null && !rawPassword.isBlank() ? rawPassword : "123456"));
                    account.setRole(roleType);
                    account.setEnabled(true);
                    account.setPasswordResetRequired(true);
                    userAccountRepository.save(account);
                    successRows++;
                    
                } catch (Exception e) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "general", e.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        
        return new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors);
    }

    @Override
    public byte[] exportUsers(String role, Boolean enabled) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户列表");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户ID");
            headerRow.createCell(1).setCellValue("用户名");
            headerRow.createCell(2).setCellValue("角色");
            headerRow.createCell(3).setCellValue("启用状态");
            headerRow.createCell(4).setCellValue("学号");
            headerRow.createCell(5).setCellValue("姓名");
            
            List<UserAccount> users = userAccountRepository.findAll();
            int rowNum = 1;
            for (UserAccount user : users) {
                if (role != null && !role.equalsIgnoreCase(user.getRole().name())) continue;
                if (enabled != null && !enabled.equals(user.getEnabled())) continue;
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(roleLabel(user.getRole()));
                row.createCell(3).setCellValue(user.getEnabled() ? "启用" : "禁用");
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportUserStats(String role, Boolean enabled, String keyword, String grade) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summary = workbook.createSheet("用户统计");
            Sheet byRole = workbook.createSheet("角色统计");

            Row header = summary.createRow(0);
            header.createCell(0).setCellValue("指标");
            header.createCell(1).setCellValue("数值");

            String normalizedKeyword = keyword == null ? null : keyword.trim();
            String normalizedGrade = grade == null ? null : grade.trim();

            int totalUsers = 0;
            int enabledUsers = 0;
            int disabledUsers = 0;
            int studentUsers = 0;
            int teacherUsers = 0;

            java.util.Map<String, Integer> roleCounter = new java.util.HashMap<>();

            List<UserAccount> users = userAccountRepository.findAll();
            for (UserAccount user : users) {
                if (user == null) {
                    continue;
                }
                if (role != null && !role.isBlank()) {
                    if (user.getRole() == null || !role.equalsIgnoreCase(user.getRole().name())) {
                        continue;
                    }
                }
                if (enabled != null && !enabled.equals(user.getEnabled())) {
                    continue;
                }

                StudentProfile profile = studentProfileRepository.findByStudentNo(user.getUsername()).orElse(null);
                if (normalizedGrade != null && !normalizedGrade.isBlank()) {
                    String userGrade = profile == null ? null : profile.getGrade();
                    if (userGrade == null || !normalizedGrade.equals(userGrade)) {
                        continue;
                    }
                }

                if (normalizedKeyword != null && !normalizedKeyword.isBlank()) {
                    boolean hit = containsIgnoreCase(user.getUsername(), normalizedKeyword)
                            || containsIgnoreCase(profile == null ? null : profile.getName(), normalizedKeyword)
                            || containsIgnoreCase(profile == null ? null : profile.getStudentNo(), normalizedKeyword);
                    if (!hit) {
                        continue;
                    }
                }

                totalUsers += 1;
                if (Boolean.TRUE.equals(user.getEnabled())) {
                    enabledUsers += 1;
                } else {
                    disabledUsers += 1;
                }

                String roleName = user.getRole() == null ? "" : user.getRole().name();
                roleCounter.put(roleName, roleCounter.getOrDefault(roleName, 0) + 1);

                boolean isStudentLike = RoleType.STUDENT.name().equals(roleName) || RoleType.LEAGUE_SECRETARY.name().equals(roleName);
                if (isStudentLike) {
                    studentUsers += 1;
                } else {
                    teacherUsers += 1;
                }
            }

            int rowIdx = 1;
            Row f0 = summary.createRow(rowIdx++);
            f0.createCell(0).setCellValue("筛选角色");
            f0.createCell(1).setCellValue(role == null || role.isBlank() ? "全部" : role);

            Row f1 = summary.createRow(rowIdx++);
            f1.createCell(0).setCellValue("筛选状态");
            f1.createCell(1).setCellValue(enabled == null ? "全部" : (enabled ? "启用" : "停用"));

            Row f2 = summary.createRow(rowIdx++);
            f2.createCell(0).setCellValue("筛选关键词");
            f2.createCell(1).setCellValue(normalizedKeyword == null || normalizedKeyword.isBlank() ? "—" : normalizedKeyword);

            Row f3 = summary.createRow(rowIdx++);
            f3.createCell(0).setCellValue("筛选年级");
            f3.createCell(1).setCellValue(normalizedGrade == null || normalizedGrade.isBlank() ? "全部" : normalizedGrade);

            Row r0 = summary.createRow(rowIdx++);
            r0.createCell(0).setCellValue("总用户数");
            r0.createCell(1).setCellValue(totalUsers);

            Row r1 = summary.createRow(rowIdx++);
            r1.createCell(0).setCellValue("启用用户数");
            r1.createCell(1).setCellValue(enabledUsers);

            Row r2 = summary.createRow(rowIdx++);
            r2.createCell(0).setCellValue("停用用户数");
            r2.createCell(1).setCellValue(disabledUsers);

            Row r3 = summary.createRow(rowIdx++);
            r3.createCell(0).setCellValue("学生端用户数");
            r3.createCell(1).setCellValue(studentUsers);

            Row r4 = summary.createRow(rowIdx++);
            r4.createCell(0).setCellValue("管理端用户数");
            r4.createCell(1).setCellValue(teacherUsers);

            Row r5 = summary.createRow(rowIdx++);
            r5.createCell(0).setCellValue("统计时间");
            r5.createCell(1).setCellValue(LocalDateTime.now().toString());

            Row roleHeader = byRole.createRow(0);
            roleHeader.createCell(0).setCellValue("角色");
            roleHeader.createCell(1).setCellValue("数量");

            int byRoleRow = 1;
            List<java.util.Map.Entry<String, Integer>> roleEntries = new java.util.ArrayList<>(roleCounter.entrySet());
            roleEntries.sort(java.util.Map.Entry.comparingByKey());
            for (java.util.Map.Entry<String, Integer> entry : roleEntries) {
                Row row = byRole.createRow(byRoleRow++);
                row.createCell(0).setCellValue(roleLabel(parseRoleSafely(entry.getKey())));
                row.createCell(1).setCellValue(entry.getValue());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public BatchImportResultResponse importStudents(MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                totalRows++;
                try {
                    String studentNo = getCellStringValue(row, 0);
                    String name = getCellStringValue(row, 1);
                    String major = getCellStringValue(row, 2);
                    String grade = getCellStringValue(row, 3);
                    String className = getCellStringValue(row, 4);
                    String email = getCellStringValue(row, 5);
                    
                    if (studentNo == null || studentNo.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "studentNo", "学号不能为空", ""));
                        continue;
                    }
                    
                    StudentProfile profile = studentProfileRepository.findByStudentNo(studentNo).orElse(null);
                    if (profile == null) {
                        profile = new StudentProfile();
                        profile.setStudentNo(studentNo);
                    }
                    
                    profile.setName(name);
                    profile.setMajor(major);
                    profile.setGrade(grade);
                    profile.setClassName(className);
                    profile.setEmail(email);
                    profile.setStatus("ACTIVE");
                    studentProfileRepository.save(profile);
                    successRows++;
                    
                } catch (Exception e) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "general", e.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        
        return new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors);
    }

    @Override
    public BatchImportResultResponse importAwardSupportRecords(MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowBlank(row, 8)) continue;

                totalRows++;
                try {
                    String studentNo = getCellStringValue(row, 0);
                    String assessmentAcademicYear = getCellStringValue(row, 1);
                    String awardName = getCellStringValue(row, 2);
                    String batchName = getCellStringValue(row, 3);
                    String awardLevel = getCellStringValue(row, 4);
                    String awardGrade = getCellStringValue(row, 5);
                    String awardAmountText = getCellStringValue(row, 6);
                    String awardType = getCellStringValue(row, 7);

                    if (studentNo == null || studentNo.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "studentNo", "学号不能为空", ""));
                        continue;
                    }
                    if (assessmentAcademicYear == null || assessmentAcademicYear.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "assessmentAcademicYear", "评定学年不能为空", studentNo));
                        continue;
                    }
                    if (awardName == null || awardName.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "awardName", "奖学金名称不能为空", studentNo));
                        continue;
                    }

                    StudentProfileResponse student = studentProfileService.getStudentByStudentNo(studentNo);
                    StudentAwardSupportRecord entity = findExistingAwardSupportRecord(
                            student.id(),
                            assessmentAcademicYear,
                            awardName,
                            batchName,
                            awardType
                    );
                    if (entity == null) {
                        entity = new StudentAwardSupportRecord();
                        entity.setStudentId(student.id());
                    }
                    entity.setAssessmentAcademicYear(assessmentAcademicYear);
                    entity.setAwardName(awardName);
                    entity.setBatchName(blankToNull(batchName));
                    entity.setAwardLevel(blankToNull(awardLevel));
                    entity.setAwardGrade(blankToNull(awardGrade));
                    entity.setAwardAmount(parseBigDecimal(awardAmountText, "奖学金额（元）"));
                    entity.setAwardType(blankToNull(awardType));
                    studentAwardSupportRecordRepository.save(entity);
                    successRows++;
                } catch (Exception e) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "general", e.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        return new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors);
    }

    @Override
    public byte[] exportStudents(String grade, String className, String status) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生列表");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("学号");
            headerRow.createCell(1).setCellValue("姓名");
            headerRow.createCell(2).setCellValue("专业");
            headerRow.createCell(3).setCellValue("年级");
            headerRow.createCell(4).setCellValue("班级");
            headerRow.createCell(5).setCellValue("邮箱");
            headerRow.createCell(6).setCellValue("状态");
            
            List<StudentProfile> students = studentProfileRepository.findAll();
            int rowNum = 1;
            for (StudentProfile student : students) {
                if (grade != null && !grade.equals(student.getGrade())) continue;
                if (className != null && !className.equals(student.getClassName())) continue;
                if (status != null && !status.equals(student.getStatus())) continue;
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentNo());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getMajor());
                row.createCell(3).setCellValue(student.getGrade());
                row.createCell(4).setCellValue(student.getClassName());
                row.createCell(5).setCellValue(student.getEmail());
                row.createCell(6).setCellValue(student.getStatus());
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    private String getCellStringValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private RoleType resolveRoleType(String raw) {
        String normalized = raw == null ? null : raw.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("empty role");
        }
        String compact = normalized.replaceAll("\\s+", "");
        String upper = compact.toUpperCase();
        for (RoleType value : RoleType.values()) {
            if (value.name().equalsIgnoreCase(upper)) {
                return value;
            }
        }
        return switch (compact) {
            case "学生" -> RoleType.STUDENT;
            case "班长" -> RoleType.CLASS_LEADER;
            case "团支书" -> RoleType.LEAGUE_SECRETARY;
            case "辅导员" -> RoleType.COUNSELOR;
            case "班主任" -> RoleType.CLASS_ADVISOR;
            case "学院管理员", "院管理员" -> RoleType.COLLEGE_ADMIN;
            case "超级管理员", "系统管理员" -> RoleType.SUPER_ADMIN;
            case "助理" -> RoleType.ASSISTANT;
            default -> throw new IllegalArgumentException("unknown role: " + compact);
        };
    }

    private String roleLabel(RoleType role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case STUDENT -> "学生";
            case CLASS_LEADER -> "班长";
            case LEAGUE_SECRETARY -> "团支书";
            case COUNSELOR -> "辅导员";
            case CLASS_ADVISOR -> "班主任";
            case COLLEGE_ADMIN -> "学院管理员";
            case SUPER_ADMIN -> "超级管理员";
            case ASSISTANT -> "助理";
        };
    }

    private RoleType parseRoleSafely(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        try {
            return RoleType.valueOf(role.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.toLowerCase().contains(keyword.toLowerCase());
    private StudentAwardSupportRecord findExistingAwardSupportRecord(Long studentId,
                                                                     String assessmentAcademicYear,
                                                                     String awardName,
                                                                     String batchName,
                                                                     String awardType) {
        return studentAwardSupportRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId).stream()
                .filter(item -> equalsText(item.getAssessmentAcademicYear(), assessmentAcademicYear))
                .filter(item -> equalsText(item.getAwardName(), awardName))
                .filter(item -> equalsText(item.getBatchName(), batchName))
                .filter(item -> equalsText(item.getAwardType(), awardType))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal parseBigDecimal(String value, String label) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (Exception ex) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private boolean equalsText(String left, String right) {
        String normalizedLeft = left == null ? "" : left.trim();
        String normalizedRight = right == null ? "" : right.trim();
        return normalizedLeft.equals(normalizedRight);
    }

    private boolean isRowBlank(Row row, int cellCount) {
        for (int i = 0; i < cellCount; i++) {
            String value = getCellStringValue(row, i);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
